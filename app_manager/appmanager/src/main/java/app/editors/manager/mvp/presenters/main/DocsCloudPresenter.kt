package app.editors.manager.mvp.presenters.main

import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.model.cloud.Access
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.Result
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.extensions.request
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Current
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.request.RequestBatchOperation
import app.documents.core.network.manager.models.request.RequestDeleteShare
import app.documents.core.network.manager.models.request.RequestFavorites
import app.documents.core.network.share.models.request.RequestRoomShare
import app.documents.core.network.share.models.request.UserIdInvitation
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.CloudFileProvider.RoomCallback
import app.documents.core.providers.FileOpenResult
import app.documents.core.providers.RoomProvider
import app.documents.core.utils.FirebaseTool
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.app.roomApi
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.DownloadReceiver.OnDownloadListener
import app.editors.manager.managers.receivers.RoomDuplicateReceiver
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.receivers.UploadReceiver.OnUploadListener
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.works.RoomDuplicateWork
import app.editors.manager.mvp.models.filter.Filter
import app.editors.manager.mvp.models.list.RecentViaLink
import app.editors.manager.mvp.models.list.Templates
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.views.main.DocsCloudView
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.fragments.main.DocsRoomFragment.Companion.TAG_DELETE_TEMPLATE
import app.editors.manager.ui.fragments.main.DocsRoomFragment.Companion.TAG_PROTECTED_ROOM_DOWNLOAD
import app.editors.manager.ui.fragments.main.DocsRoomFragment.Companion.TAG_PROTECTED_ROOM_OPEN_FOLDER
import app.editors.manager.ui.fragments.main.DocsRoomFragment.Companion.TAG_PROTECTED_ROOM_SHOW_INFO
import app.editors.manager.ui.fragments.main.ToolbarState
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.viewModels.main.CopyItems
import app.editors.manager.viewModels.main.TemplateSettingsMode
import app.editors.manager.viewModels.main.VersionViewer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.tools.FileExtensions
import lib.toolkit.base.managers.utils.EditType
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.Extension
import moxy.InjectViewState
import moxy.presenterScope
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class DocsCloudPresenter(private val account: CloudAccount) : DocsBasePresenter<DocsCloudView, CloudFileProvider>(),
    OnDownloadListener, OnUploadListener, RoomDuplicateReceiver.Listener, VersionViewer {

    @Inject
    lateinit var downloadReceiver: DownloadReceiver

    @Inject
    lateinit var firebaseTool: FirebaseTool

    private val uploadReceiver: UploadReceiver = UploadReceiver()
    private var duplicateRoomReceiver: RoomDuplicateReceiver = RoomDuplicateReceiver()

    private var api: ManagerService? = null
    private var roomProvider: RoomProvider? = null

    private var conversionJob: Job? = null

    init {
        App.getApp().appComponent.inject(this)
        api = context.api
        roomProvider = context.roomProvider
        fileProvider = context.cloudFileProvider.apply {
            roomCallback = object : RoomCallback {

                override fun isRoomRoot(id: String?): Boolean {
                    val parts = modelExplorerStack.last()?.pathParts.orEmpty()
                    return if (parts.isNotEmpty()) isRoom && parts[0].id == id else false
                }

                override fun isArchive(): Boolean = ApiContract.SectionType.isArchive(currentSectionType)

                override fun isRecent(): Boolean {
                    return modelExplorerStack.rootFolderType == ApiContract.SectionType.CLOUD_RECENT
                }

                override fun isTemplatesRoot(id: String?) =
                    isTemplatesFolder && modelExplorerStack.last()?.pathParts?.firstOrNull()?.id == id
            }
        }

        if (folderId != null) {
            modelExplorerStack.addStack(Explorer(current = Current().apply { id = folderId!! }))
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        downloadReceiver.addListener(this)
        uploadReceiver.setOnUploadListener(this)
        duplicateRoomReceiver.setListener(this)
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(duplicateRoomReceiver, RoomDuplicateReceiver.getFilters())
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(uploadReceiver, uploadReceiver.filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        interruptConversion()
        downloadReceiver.removeListener(this)
        uploadReceiver.setOnUploadListener(null)
        duplicateRoomReceiver.setListener(null)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(uploadReceiver)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(duplicateRoomReceiver)
    }

    override fun onItemClick(item: Item, position: Int) {
        onClickEvent(item, position)
        itemClicked?.let { itemClicked ->
            if (isSelectionMode) {
                val pickerMode = this.pickerMode
                if (pickerMode is PickerMode.Files) {
                    if (itemClicked is CloudFolder) {
                        openFolder(itemClicked.id, position)
                    } else if (itemClicked is CloudFile) {
                        if (itemClicked.isPdfForm) pickerMode.selectId(itemClicked.id)
                        modelExplorerStack.setSelectById(item, !itemClicked.isSelected)
                        viewState.onStateUpdateSelection(true)
                        viewState.onItemSelected(
                            position,
                            pickerMode.selectedIds.size.toString()
                        )
                    }
                    return
                }
                modelExplorerStack.setSelectById(item, !itemClicked.isSelected)
                if (!isSelectedItemsEmpty) {
                    viewState.onStateUpdateSelection(true)
                    viewState.onItemSelected(
                        position,
                        modelExplorerStack.countSelectedItems.toString()
                    )
                }
            } else if (isTrashMode && currentSectionType != ApiContract.SectionType.CLOUD_ARCHIVE_ROOM) {
                viewState.onSnackBarWithAction(
                    context.getString(R.string.trash_snackbar_move_text),
                    context.getString(R.string.trash_snackbar_move_button)
                ) { moveCopySelected(OperationsState.OperationType.RESTORE) }
            } else {
                if (itemClicked is CloudFolder) {
                    if (itemClicked.isRoom && itemClicked.passwordProtected) {
                        viewState.onRoomViaLinkPasswordRequired(false, TAG_PROTECTED_ROOM_OPEN_FOLDER)
                    } else {
                        openFolder(itemClicked.id, position)
                    }
                } else if (itemClicked is CloudFile) {
                    if (FileExtensions.isOpenFormat(itemClicked.clearExt)) {
                        viewState.onConversionQuestion()
                    } else {
                        if (StringUtils.getExtension(itemClicked.fileExst) == Extension.PDF) {
                            openFillFormFile()
                        } else {
                            openFile(EditType.Edit())
                        }
                    }
                } else if (itemClicked is RecentViaLink) {
                    openRecentViaLink()
                } else if (itemClicked is Templates){
                    openTemplates()
                }
            }
        }
    }

    override fun copy(): Boolean {
        if (!checkFillFormsRoom()) {
            return false
        }

        if (pickerMode is PickerMode.Files || super.copy()) {
            checkMoveCopyFiles(MoveCopyDialog.ACTION_COPY)
            return true
        }

        return false
    }

    override fun move(): Boolean {
        if (!checkFillFormsRoom()) {
            return false
        }

        return if (super.move()) {
            checkMoveCopyFiles(MoveCopyDialog.ACTION_MOVE)
            true
        } else {
            false
        }
    }

    fun copyFilesToCurrent() {
        val pickerMode = this.pickerMode
        if (pickerMode is PickerMode.Files) {
            val request = RequestBatchOperation(destFolderId = pickerMode.destFolderId).apply {
                fileIds = pickerMode.selectedIds
            }
            disposable.add(fileProvider.copyFiles(request).subscribe({ onBatchOperations() }, ::fetchError))
        }
    }

    private fun checkFillFormsRoom(): Boolean {
        val explorer = operationStack?.explorer ?: return false
        if (roomClicked?.roomType == ApiContract.RoomType.FILL_FORMS_ROOM) {
            if (explorer.folders.isNotEmpty() || explorer.files.any { !it.isPdfForm }) {
                viewState.onDialogWarning(
                    context.getString(R.string.dialogs_warning_title),
                    context.getString(R.string.dialogs_warning_only_pdf_form_message),
                    null
                )
                return false
            }
        }
        return true
    }

    override fun getNextList() {
        val id = modelExplorerStack.currentId
        val loadPosition = modelExplorerStack.loadPosition
        if (id != null && loadPosition > 0) {
            val args = getArgs(filteringValue).toMutableMap()
            args[ApiContract.Parameters.ARG_START_INDEX] = loadPosition.toString()
            disposable.add(fileProvider.getFiles(id, args.putFilters()).subscribe({ explorer: Explorer? ->
                modelExplorerStack.addOnNext(explorer)
                val last = modelExplorerStack.last()
                if (last != null) {
                    viewState.onDocsNext(getListWithHeaders(last, true))
                }
            }) { throwable: Throwable -> fetchError(throwable) })
        }
    }

    override fun createDocs(title: String) {
        FirebaseUtils.addAnalyticsCreateEntity(
            account.portalUrl,
            true,
            StringUtils.getExtensionFromPath(title)
        )
        super.createDocs(title)
    }

    override fun openFileVersion(file: CloudFile, onError: (Throwable) -> Unit){
        openFileJob = presenterScope.launch {
            fileProvider.openFile(
                id = file.id,
                version = file.version,
                editType = EditType.Edit(),
                canBeShared = false
            ).collect(::onFileOpenCollect)
        }
    }

    override fun updateViewsState() {
        if (isSelectionMode) {
            viewState.onStateUpdateSelection(true)
            if (pickerMode is PickerMode.Files) {
                viewState.onActionBarTitle((pickerMode as PickerMode.Files).selectedIds.size.toString())
            } else {
                viewState.onActionBarTitle(modelExplorerStack.countSelectedItems.toString())
            }
            viewState.onStateAdapterRoot(modelExplorerStack.isNavigationRoot)
            viewState.onStateActionButton(false)
        } else if (isFilteringMode) {
            viewState.onActionBarTitle(context.getString(R.string.toolbar_menu_search_result))
            viewState.onStateUpdateFilter(true, filteringValue)
            viewState.onStateAdapterRoot(modelExplorerStack.isNavigationRoot)
            viewState.onStateActionButton(false)
        } else if (!modelExplorerStack.isRoot) {
            viewState.onStateAdapterRoot(false)
            viewState.onStateUpdateRoot(false)
            // TODO check security...
            if (isRoom) {
                viewState.onStateActionButton(modelExplorerStack.last()?.current?.security?.create == true)
            } else {
                viewState.onStateActionButton(isContextEditable && !isRecentViaLinkSection())
            }
            viewState.onActionBarTitle(currentTitle)
        } else {
            when {
                isTrashMode -> {
                    viewState.onStateActionButton(false)
                    viewState.onActionBarTitle("")
                }

                pickerMode == PickerMode.Folders -> {
                    if (isRoom && isRoot) {
                        viewState.onActionBarTitle(context.getString(R.string.operation_select_room_title))
                    } else {
                        viewState.onActionBarTitle(context.getString(R.string.operation_title))
                    }
                    viewState.onStateActionButton(false)
                }

                else -> {
                    viewState.onActionBarTitle("")
                    if (isRoom && modelExplorerStack.last()?.current?.security?.create == true) {
                        viewState.onStateActionButton(true)
                    } else {
                        viewState.onStateActionButton(isContextEditable)
                    }
                }
            }
            viewState.onStateAdapterRoot(true)
            viewState.onStateUpdateRoot(true)
        }
        val lifetime = currentFolder?.lifetime
        val toolbarState = when {
            currentFolder?.isTemplate == true -> ToolbarState.RoomTemplate
            lifetime != null -> ToolbarState.RoomLifetime(lifetime)
            else -> ToolbarState.None
        }
        viewState.setToolbarState(toolbarState)
        viewState.onRoomFileIndexing(isIndexing)
    }

    override fun onActionClick() {
        viewState.onActionDialog(
            isRoot && (isUserSection || isCommonSection && isAdmin),
            !isVisitor || modelExplorerStack.last()?.current?.security?.create == true,
            roomClicked?.roomType
        )
    }

    /*
     * Loading callbacks
     * */
    override fun onDownloadError(info: String?) {
        viewState.onDialogClose()
        viewState.onSnackBar(info ?: context.getString(R.string.download_manager_error))
    }

    override fun onDownloadProgress(id: String?, total: Int, progress: Int) {
        viewState.onDialogProgress(total, progress)
    }

    override fun onDownloadComplete(
        id: String?,
        url: String?,
        title: String?,
        info: String?,
        path: String?,
        mime: String?,
        uri: Uri?,
    ) {
        viewState.onFinishDownload(uri)
        viewState.onDialogClose()
        viewState.onSnackBarWithAction("$info\n$title", context.getString(R.string.download_manager_open)) {
            uri?.let(::showDownloadFolderActivity)
        }
    }

    override fun onDownloadCanceled(id: String?, info: String?) {
        viewState.onDialogClose()
        viewState.onSnackBar(info)
    }

    override fun onDownloadRepeat(id: String?, title: String?, info: String?) {
        viewState.onDialogClose()
        viewState.onSnackBar(info)
    }

    override fun onUploadError(path: String?, info: String?, file: String?) {
        viewState.onSnackBar(info)
    }

    override fun onUploadErrorDialog(title: String, message: String, file: String?) {
        viewState.onDialogWarning(title, message, null)
    }

    override fun onHideDuplicateNotification(workerId: String?) {
        WorkManager
            .getInstance(context)
            .cancelWorkById(UUID.fromString(workerId))
    }

    override fun onDuplicateComplete() {
        if (isRoom && isRoot) refresh()
    }

    override fun onUploadComplete(
        path: String?,
        info: String?,
        title: String?,
        file: CloudFile?,
        id: String?,
    ) {
        if (modelExplorerStack.currentId == file?.folderId) {
            addFile(file)
        }
        viewState.onSnackBar(info)
    }

    override fun onUploadAndOpen(path: String?, title: String?, file: CloudFile?, id: String?) {
        viewState.onFileWebView(checkNotNull(file))
    }

    override fun onUploadFileProgress(progress: Int, id: String?, folderId: String?) {
        // Nothing
    }

    override fun onUploadCanceled(path: String?, info: String?, id: String?) {
        viewState.onSnackBar(info)
    }

    override fun onUploadRepeat(path: String?, info: String?) {
        viewState.onDialogClose()
        viewState.onSnackBar(info)
    }

    override fun getBackStack(): Boolean {
        if ((currentFolder?.isTemplate == true || isTemplatesFolder)
            && !isSelectionMode && !isFilteringMode
        ) {
            resetFilters()
            return super.getBackStack()
        }
        val backStackResult = super.getBackStack()
        if (modelExplorerStack.last()?.filterType != preferenceTool.filter.type.filterVal) {
            refresh()
        }
        return backStackResult
    }

    override fun openFolder(id: String?, position: Int, roomType: Int?) {
        setFiltering(false)
        resetFilters()
        super.openFolder(id, position, roomType)
    }

    override fun createDownloadFile() {
        if (isRoom && isRoot && roomClicked?.passwordProtected == true) {
            viewState.onRoomViaLinkPasswordRequired(false, TAG_PROTECTED_ROOM_DOWNLOAD)
            return
        }
        super.createDownloadFile()
    }

    fun openFillFormFile() {
        val file = itemClicked as? CloudFile ?: return

        presenterScope.launch {
            if (file.formFillingStatus == ApiContract.FormFillingStatus.YourTurn &&
                !firebaseTool.isCoauthoring()
            ) {
                viewState.showFillFormIncompatibleVersionsDialog()
                return@launch
            }

            if (account.portal.isDocSpace && file.isPdfForm && isUserSection) {
                viewState.showFillFormChooserFragment()
                return@launch
            }

            openFile(EditType.Fill())
        }
    }

    override fun openFile(editType: EditType, canBeShared: Boolean) {
        when {
            isTemplatesFolder -> editTemplate()
            itemClicked is CloudFolder -> editRoom()
            else -> super.openFile(editType, isItemShareable)
        }
    }

    override suspend fun openFileAndCollect(
        cloudFile: CloudFile,
        editType: EditType,
        canBeShared: Boolean
    ) {
        fileProvider.openFile(
            cloudFile = cloudFile,
            editType = editType,
            canBeShared = canBeShared,
            access = cloudFile.access
        ).collect(::onFileOpenCollect)
    }

    override suspend fun onFileOpenCollect(result: FileOpenResult) {
        if (result !is FileOpenResult.Loading) viewState.onDialogClose(true)
        when (result) {
            is FileOpenResult.OpenDocumentServer -> {
                viewState.onOpenDocumentServer(result.cloudFile, result.info, result.editType)
                FirebaseUtils.addAnalyticsOpenEntity(account.portalUrl, result.cloudFile.fileExst)
            }
            else -> super.onFileOpenCollect(result)
        }
    }

    fun saveExternalLinkToClipboard() {
        itemClicked?.let { item ->
            presenterScope.launch {
                request(
                    func = { context.shareApi.getShareFile(item.id) },
                    map = { response ->
                        response.response.find { it.sharedTo.shareLink.isNotEmpty() }?.sharedTo?.shareLink ?: ""
                    },
                    onSuccess = { externalLink ->
                        if (externalLink.isNotEmpty()) {
                            setDataToClipboard(externalLink, (item as? CloudFile)?.customFilterEnabled)
                        } else {
                            viewState.onSnackBar(context.getString(R.string.share_access_denied))
                        }
                    }, onError = ::fetchError
                )
            }
        }
    }

    fun addToFavorite() {
        val requestFavorites = RequestFavorites()
        requestFavorites.fileIds = listOf(itemClicked?.id!!)
        val item = itemClicked
        if (item != null && item is CloudFile) {
            val isAdd = !item.isFavorite
            disposable.add(fileProvider.addToFavorites(requestFavorites, isAdd)
                .subscribe({
                    if (isAdd) {
                        item.fileStatus += ApiContract.FileStatus.FAVORITE
                    } else {
                        item.fileStatus -= ApiContract.FileStatus.FAVORITE
                    }
                    viewState.onUpdateItemState()
                    viewState.onSnackBar(
                        if (isAdd) {
                            context.getString(R.string.operation_add_to_favorites)
                        } else {
                            context.getString(R.string.operation_remove_from_favorites)
                        }
                    )
                }) { throwable: Throwable -> fetchError(throwable) })
        }
    }

    fun removeShareContext() {
        if (itemClicked != null) {
            val deleteShare = RequestDeleteShare()
            if (itemClicked is CloudFolder) {
                deleteShare.folderIds = ArrayList(listOf((itemClicked as CloudFolder).id))
            } else {
                deleteShare.fileIds = ArrayList(listOf(itemClicked!!.id))
            }
            disposable.add(Observable.fromCallable {
                api?.deleteShare(deleteShare)?.execute()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (itemClicked != null) {
                        modelExplorerStack.removeItemById(itemClicked?.id)
                    }
                    setPlaceholderType(
                        if (modelExplorerStack.isListEmpty)
                            PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE
                    )
                    viewState.onDocsGet(getListWithHeaders(modelExplorerStack.last(), true))
                    onBatchOperations()
                }) { throwable: Throwable -> fetchError(throwable) })
        }
    }

    fun emptyTrash() {
        val explorer = modelExplorerStack.last()
        if (explorer != null) {
            showDialogProgress(true, TAG_DIALOG_CANCEL_BATCH_OPERATIONS)
            batchDisposable = fileProvider.clearTrash()
                .switchMap { status }
                .subscribe(
                    { progress: Int? ->
                        viewState.onDialogProgress(
                            FileUtils.LOAD_MAX_PROGRESS,
                            progress!!
                        )
                    },
                    { throwable: Throwable -> fetchError(throwable) }
                ) {
                    onBatchOperations()
                    refresh()
                }
        }
    }

    private fun setDataToClipboard(value: String, customFilterEnabled: Boolean? = false) {
        KeyboardUtils.setDataToClipboard(
            context,
            value,
            context.getString(R.string.share_clipboard_external_link_label)
        )
        viewState.onSnackBar(
            context.getString(
                if (customFilterEnabled == true) {
                    R.string.share_clipboard_external_custom_filter_copied
                } else {
                    R.string.share_clipboard_external_copied
                }
            )
        )
    }

    private fun checkMoveCopyFiles(action: String) {
        val filesIds = (pickerMode as? PickerMode.Files)?.selectedIds ?: operationStack?.selectedFilesIds
        val foldersIds = operationStack?.selectedFoldersIds

        api?.let { api ->
            disposable.add(api.checkFiles(
                (pickerMode as? PickerMode.Files)?.destFolderId ?: destFolderId ?: "",
                foldersIds,
                filesIds
            )
                .map { it.response }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when {
                        it.isNotEmpty() -> {
                            showMoveCopyDialog(it, action, modelExplorerStack.currentTitle)
                        }

                        action == MoveCopyDialog.ACTION_COPY -> {
                            transfer(ApiContract.Operation.DUPLICATE, false)
                        }

                        action == MoveCopyDialog.ACTION_MOVE -> {
                            transfer(ApiContract.Operation.DUPLICATE, true)
                        }
                    }
                }, ::fetchError)
            )
        }
    }

    private fun showMoveCopyDialog(files: List<CloudFile>, action: String, titleFolder: String) {
        val names = ArrayList<String>()
        for (file in files) {
            names.add(file.title)
        }
        viewState.showMoveCopyDialog(names, action, titleFolder)
    }

    private fun resetFilters() {
        preferenceTool.filter = Filter()
        viewState.onStateUpdateFilterMenu()
    }

    fun openFileById(id: String, editType: EditType) {
        openFile(CloudFile().apply { this.id = id }, editType)
    }

    fun openFile(data: String) {
        val model = Json.decodeFromString<OpenDataModel>(data)
        val fileId = model.file?.id
        val folderId = model.folder?.id

        if (fileId == null && folderId != null) {
            openFolder(folderId, 0)
            return
        }

        openFileJob?.cancel()
        openFileJob = presenterScope.launch {
            if (model.share.isNotEmpty() && !model.portal.isNullOrEmpty()) {
                fileProvider.openFile(
                    portal = model.portal,
                    token = model.share,
                    id = fileId.orEmpty(),
                    title = model.file?.title.orEmpty(),
                    extension = model.file?.extension.orEmpty(),
                ).collect(::onFileOpenCollect)
            } else {
                fileProvider.openFile(
                    cloudFile = CloudFile().apply { this.id = fileId.orEmpty() },
                    editType = EditType.from(model.action),
                    canBeShared  = false
                ).collect(::onFileOpenCollect)
            }
        }
    }

    fun openLocation() {
        resetFilters()
        if (folderId != itemFolderId) {
            setFiltering(false)
            modelExplorerStack.previous()?.let(modelExplorerStack::refreshStack)
            getItemsById(itemFolderId)
        } else if (isRoot) {
            getBackStack()
        }
    }

    /*
     * Getter/Setters for states
     * */
    private val isAdmin: Boolean
        get() = account.isAdmin

    private val isVisitor: Boolean
        get() = account.isVisitor

    /*
     * A&(B&(Cv(D&!E)))v((FvGvH)&D&!E)
     * */
    private val isContextEditable: Boolean
        get() = isUserSection || isCommonSection || isRoom && (isAdmin || isContextReadWrite && !isRoot) ||
                (isShareSection || isProjectsSection || isBunchSection) && isContextReadWrite && !isRoot

    /*
     * I&(!K&!F&!BvJ)
     * */
    val isContextItemEditable: Boolean
        get() = isContextEditable && (!isVisitor && !isShareSection || isCommonSection || isItemOwner)

    val areItemsRemovable: Boolean
        get() = modelExplorerStack.selectedFolders.none { it.security?.delete == false }

    val isContextOwner: Boolean
        get() = StringUtils.equals(modelExplorerStack.currentFolderOwnerId, account.id)

    private val isContextReadWrite: Boolean
        get() = isContextOwner || modelExplorerStack.currentFolderAccess == Access.Read.code ||
                modelExplorerStack.currentFolderAccess == Access.None.code

    val isUserSection: Boolean
        get() = currentSectionType == ApiContract.SectionType.CLOUD_USER

    private val isShareSection: Boolean
        get() = currentSectionType == ApiContract.SectionType.CLOUD_SHARE

    private val isCommonSection: Boolean
        get() = currentSectionType == ApiContract.SectionType.CLOUD_COMMON

    private val isProjectsSection: Boolean
        get() = currentSectionType == ApiContract.SectionType.CLOUD_PROJECTS

    private val isBunchSection: Boolean
        get() = currentSectionType == ApiContract.SectionType.CLOUD_BUNCH

    private val isTrashSection: Boolean
        get() = currentSectionType == ApiContract.SectionType.CLOUD_TRASH ||
                currentSectionType == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM

    private val isRoom: Boolean
        get() = currentSectionType > ApiContract.SectionType.CLOUD_PRIVATE_ROOM

    private val isItemOwner: Boolean
        get() = StringUtils.equals(itemClicked?.createdBy?.id, account.id)

    private val isItemReadWrite: Boolean
        get() = itemClicked?.access == Access.ReadWrite || isUserSection

    private val isItemEditable: Boolean
        get() = if (account.isDocSpace && currentSectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM) {
            itemClicked?.isCanEdit == true
        } else {
            !isVisitor && !isProjectsSection && (isItemOwner || isItemReadWrite ||
                    itemClicked?.access in listOf(Access.Review, Access.FormFiller, Access.Comment))
        }

    private val isItemShareable: Boolean
        get() = if (account.isDocSpace && (currentSectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM || currentSectionType == ApiContract.SectionType.CLOUD_USER)) {
            itemClicked?.isCanShare == true
        } else {
            isItemEditable && (!isCommonSection || isAdmin) && !isProjectsSection
                    && !isBunchSection && isItemReadWrite
        }

    private val isClickedItemStorage: Boolean
        get() = itemClicked?.providerItem == true

    private val itemFolderId: String?
        get() = (itemClicked as? CloudFile)?.folderId ?: (itemClicked as? CloudFolder)?.parentId

    val isCurrentRoom: Boolean
        get() = currentSectionType > ApiContract.SectionType.CLOUD_PRIVATE_ROOM // && modelExplorerStack.last()?.current?.isCanEdit == true

    private fun showDownloadFolderActivity(uri: Uri) {
        viewState.onDownloadActivity(uri)
    }

    fun archiveRooms(isArchive: Boolean) {
        viewState.onDialogProgress(
            context.getString(R.string.dialogs_wait_title),
            true,
            TAG_DIALOG_CANCEL_SINGLE_OPERATIONS
        )
        viewState.onDialogProgress(100, 0)
        requestJob = presenterScope.launch(Dispatchers.IO) {
            try {
                val provider = requireNotNull(roomProvider)
                val message = if (isSelectionMode) {
                    val selected = modelExplorerStack.selectedFolders.map(CloudFolder::id)
                    selected.forEachIndexed { index, id ->
                        provider.archiveRoom(id, isArchive)
                        withContext(Dispatchers.Main) {
                            val progress = 100 / (selected.size / (index + 1).toFloat())
                            viewState.onDialogProgress(100, progress.toInt())
                        }
                    }
                    if (isArchive) {
                        context.getString(R.string.context_rooms_archive_message)
                    } else {
                        context.resources.getQuantityString(R.plurals.context_rooms_unarchive_message, selected.size)
                    }
                } else {
                    provider.archiveRoom(roomClicked?.id.orEmpty(), isArchive = isArchive)
                    if (isArchive) {
                        context.getString(R.string.context_room_archive_message)
                    } else {
                        context.resources.getQuantityString(R.plurals.context_rooms_unarchive_message, 1)
                    }
                }

                withContext(Dispatchers.Main) {
                    if (isSelectionMode) {
                        deselectAll()
                        setSelection(false)
                    }
                    viewState.onDialogProgress(100, 100)
                    viewState.onSnackBar(message)
                    if (isTrashSection) { popToRoot() }
                    refresh()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (e !is CancellationException) {
                        fetchError(e)
                    }
                }
            } finally {
                viewState.onDialogClose()
            }
        }
    }

    fun copyLinkFromActionMenu(isRoom: Boolean) {
        if (isRoom) {
            copyRoomLink()
        } else {
            (itemClicked as? CloudFolder)?.let { saveLink(getInternalLink(it)) }
        }
    }

    fun copyLinkFromContextMenu() {
        val item = itemClicked
        when {
            (item as? CloudFolder)?.isRoom == true -> copyRoomLink()
            item is CloudFolder -> saveLink(getInternalLink(item))
            else -> saveExternalLinkToClipboard()
        }
    }

    fun pinRoom() {
        roomProvider?.let {
            itemClicked?.let { folder ->
                if (folder is CloudFolder) {
                    disposable.add(
                        it.pinRoom(folder.id, !folder.pinned)
                            .doOnSubscribe { viewState.onSwipeEnable(true) }
                            .subscribe({ response ->
                                if (response.statusCode.toInt() == ApiContract.HttpCodes.SUCCESS) {
                                    folder.pinned = !folder.pinned
                                    viewState.onUpdateItemState()
                                }
                            }, ::fetchError)
                    )
                }
            }
        }
    }

    fun lockFile() {
        val roomProvider = roomProvider ?: return
        val file = itemClicked as? CloudFile ?: return
        presenterScope.launch {
            roomProvider.lockFile(id = file.id, lock = !file.isLocked)
                .collect { result ->
                    when (result) {
                        is Result.Success -> refresh()
                        is Result.Error -> fetchError(result.exception)
                    }
                }
        }
    }

    fun setCustomFilter() {
        val roomProvider = roomProvider ?: return
        val file = itemClicked as? CloudFile ?: return
        presenterScope.launch {
            val enable = !file.customFilterEnabled
            roomProvider.enableCustomFilter(id = file.id, enable = enable)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            file.customFilterEnabled = enable
                            viewState.onUpdateItemState()
                            viewState.onSnackBar(
                                if (enable) {
                                    context.getString(R.string.operation_enable_custom_filter)
                                } else {
                                    context.getString(R.string.operation_disable_custom_filter)
                                }
                            )
                        }
                        is Result.Error -> fetchError(result.exception)
                    }
                }
        }
    }

    fun showVersionHistory() {
        (itemClicked as? CloudFile)?.let {
            viewState.showVersionHistoryFragment(it.id)
        }
    }

    fun createTemplate() {
        (itemClicked as? CloudFolder)?.let { folder ->
            viewState.showTemplateSettingsFragment(
                folder.id,
                TemplateSettingsMode.MODE_CREATE_TEMPLATE
            )
        }
    }

    fun createRoomFromTemplate() {
        roomClicked?.let { template ->
            viewState.showRoomFromTemplateFragment(template.id)
        }
    }

    fun editTemplate() {
        roomClicked?.let { template ->
            viewState.showTemplateSettingsFragment(
                template.id,
                TemplateSettingsMode.MODE_EDIT_TEMPLATE
            )
        }
    }

    fun editTemplateAccessSettings() {
        roomClicked?.let { folder ->
            viewState.showTemplateAccessSettingsFragment(folder.id)
        }
    }

    fun showTemplateInfo() {
        roomClicked?.let { folder ->
            viewState.showTemplateInfoFragment(folder.id)
        }
    }

    fun createRoom(roomType: Int) {
        val files = modelExplorerStack.selectedFiles.toMutableList()
        val folders = modelExplorerStack.selectedFolders.toMutableList()
        val clickedItem = itemClicked

        deselectAll()
        if (files.isEmpty() && folders.isEmpty() && clickedItem != null) {
            if (clickedItem is CloudFolder) {
                folders.add(clickedItem)
            } else if (clickedItem is CloudFile) {
                files.add(clickedItem)
            }
        }

        if (roomType == ApiContract.RoomType.FILL_FORMS_ROOM) {
            if (folders.isNotEmpty() || files.any { !it.isPdfForm }) {
                viewState.onDialogWarning(
                    context.getString(R.string.dialogs_warning_title),
                    context.getString(R.string.dialogs_warning_fill_forms_room_create),
                    null
                )
                return
            }
        }

        viewState.showAddRoomFragment(
            type = roomType,
            copyItems = CopyItems(
                folderIds = folders.map(CloudFolder::id),
                fileIds = files.map(CloudFile::id)
            )
        )
    }

    fun editRoom() {
        roomClicked?.let { room ->
            viewState.showEditRoomFragment(room)
        }
    }

    fun deleteRoom() {
        if (isSelectionMode && modelExplorerStack.countSelectedItems > 0) {
            roomProvider?.let { provider ->
                disposable.add(
                    provider.deleteRoom(items = modelExplorerStack.selectedFolders.map(CloudFolder::id))
                        .subscribe({
                            viewState.onDialogClose()
                            viewState.onSnackBar(context.getString(R.string.room_delete_success))
                            deselectAll()
                            refresh()
                        }) { fetchError(it) }
                )
            }
        } else if (itemClicked != null) {
            roomProvider?.let { provider ->
                disposable.add(
                    provider.deleteRoom(itemClicked?.id ?: "")
                        .doOnComplete { popToRoot() }
                        .delay(1000, TimeUnit.MILLISECONDS)
                        .subscribe({
                            viewState.onDialogClose()
                            viewState.onSnackBar(context.getString(R.string.room_delete_success))
                            refresh()
                        }, ::fetchError)
                )
            }
        }
    }

    fun onDeleteTemplates(){
        if (modelExplorerStack.countSelectedItems > 0){
            viewState.onDialogDelete(
                modelExplorerStack.countSelectedItems,
                true,
                TAG_DELETE_TEMPLATE
            )
        }
    }

    fun deleteTemplate() {
        showDialogProgress(true, TAG_DIALOG_CANCEL_BATCH_OPERATIONS)
        fun onSuccess(msgId: Int) {
            viewState.onDialogProgress(100, 100)
            viewState.onDialogClose()
            viewState.onSnackBar(context.getString(msgId))
            if (currentFolder?.isTemplate == true) getBackStack()
            refresh()
        }

        if (isSelectionMode && modelExplorerStack.countSelectedItems > 0) {
            disposable.add(
                fileProvider.delete(modelExplorerStack.selectedFolders, null)
                    .subscribe({
                        deselectAll()
                        onSuccess(R.string.templates_delete_success)
                    }, ::fetchError)
            )
        } else {
            roomClicked?.let { template ->
                disposable.add(
                    fileProvider.delete(listOf(template), null)
                        .subscribe({ onSuccess(R.string.template_delete_success) }, ::fetchError)
                )
            }
        }
    }

    fun interruptConversion(): Boolean {
        if (conversionJob?.isCompleted == true) return false
        val cancelled = conversionJob?.isCancelled == true // conversionJob == null || isCancelled
        conversionJob?.cancel()
        return cancelled
    }

    fun convertToOOXML() {
        val extension = FileExtensions.toOOXML((itemClicked as CloudFile).fileExst)
        viewState.onConversionProgress(0, extension)
        conversionJob = presenterScope.launch {
            try {
                fileProvider.convertToOOXML(itemClicked?.id.orEmpty()).collectLatest {
                    withContext(Dispatchers.Main) {
                        viewState.onConversionProgress(it, extension)
                        if (it == 100) {
                            delay(300L)
                            viewState.onDialogClose()
                            refresh()
                            viewState.onScrollToPosition(0)
                            conversionJob?.cancel()
                        }
                    }
                }
            } catch (error: Throwable) {
                if (conversionJob?.isCancelled == true) return@launch
                viewState.onDialogClose()
                fetchError(error)
            }
        }
    }

    fun checkRoomOwner() {
        if (roomClicked != null) {
            viewState.onLeaveRoomDialog(
                R.string.leave_room_title,
                if (isItemOwner) R.string.leave_room_owner_desc else R.string.leave_room_desc,
                isItemOwner
            )
        }
    }

    fun leaveRoom() {
        if (!isItemOwner) {
            showDialogWaiting(null)
            presenterScope.launch(Dispatchers.IO) {
                try {
                    context.roomApi.shareRoom(
                        id = roomClicked?.id ?: "",
                        body = RequestRoomShare(
                            invitations = listOf(
                                UserIdInvitation(
                                    id = account.id,
                                    access = Access.None.code
                                )
                            )
                        )
                    )
                    withContext(Dispatchers.Main) {
                        viewState.onDialogClose()
                        viewState.onSnackBar(context.getString(R.string.leave_room_message))
                        refresh()
                    }
                } catch (e: Exception) {
                    viewState.onDialogClose()
                    fetchError(e)
                }
            }
        } else {
            viewState.showSetOwnerFragment(roomClicked ?: error("room can not be null"))
        }
    }

    fun tryMove() {
        val item = itemClicked
        if (item is CloudFolder && item.roomType == ApiContract.RoomType.PUBLIC_ROOM) {
            viewState.onDialogQuestion(
                context.getString(R.string.rooms_move_to_public_title),
                context.getString(R.string.rooms_move_to_public_title_desc),
                TAG_DIALOG_MOVE_TO_PUBLIC
            )
        } else {
            move()
        }
    }

    fun getSelectedItemsCount(): Int {
        return modelExplorerStack.countSelectedItems
    }

    // use for operation in order to filter by room
    fun setFilterByRoom(roomType: Int) {
        filters = mapOf(ApiContract.Parameters.ARG_FILTER_BY_TYPE_ROOM to roomType.toString())
        fileProvider.roomCallback = object : RoomCallback {
            override fun isRoomRoot(id: String?): Boolean {
                val parts = modelExplorerStack.last()?.pathParts.orEmpty()
                return if (parts.isNotEmpty()) {
                    parts[0].id == id
                } else {
                    modelExplorerStack.isStackEmpty || modelExplorerStack.isRoot
                }
            }
            override fun isArchive(): Boolean = false
            override fun isRecent(): Boolean = false
            override fun isTemplatesRoot(id: String?): Boolean = isTemplatesFolder

        }
    }

    fun duplicateRoom() {
        val workData = Data.Builder()
            .putString(RoomDuplicateWork.KEY_ROOM_ID, roomClicked?.id)
            .putString(RoomDuplicateWork.KEY_ROOM_TITLE, roomClicked?.title)
            .build()

        val request = OneTimeWorkRequest.Builder(RoomDuplicateWork::class.java)
            .addTag(RoomDuplicateWork.getTag(roomClicked?.id.hashCode(), roomClicked?.title))
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    private fun openRecentViaLink() {
        setPlaceholderType(PlaceholderViews.Type.LOAD)
        (fileProvider as? CloudFileProvider)?.let { provider ->
            disposable.add(
                provider.getRecentViaLink()
                    .subscribe({ explorer ->
                        loadSuccess(explorer.apply { current.title = context.getString(R.string.room_access_via_link_title) })
                    }, ::fetchError)
            )
        }
    }

    private fun openTemplates() {
        val cloudProvider = (fileProvider as? CloudFileProvider) ?: return
        resetFilters()
        setPlaceholderType(PlaceholderViews.Type.LOAD)
        disposable.add(
            cloudProvider.getRoomTemplates(getArgs(filteringValue).putFilters())
                .subscribe(::loadSuccess, ::fetchError)
        )
    }

    private fun copyRoomLink() {
        roomClicked?.let { room ->
            if (isTemplatesFolder || room.roomType == ApiContract.RoomType.COLLABORATION_ROOM
                || room.roomType == ApiContract.RoomType.VIRTUAL_ROOM
            ) {
                setDataToClipboard(getInternalLink(room))
            } else {
                presenterScope.launch {
                    try {
                        val externalLink = roomProvider?.getExternalLink(roomClicked?.id.orEmpty())
                        withContext(Dispatchers.Main) {
                            if (externalLink.isNullOrEmpty()) {
                                viewState.onError(context.getString(R.string.errors_unknown_error))
                            } else {
                                saveLink(externalLink)
                            }
                        }
                    } catch (error: Throwable) {
                        fetchError(error)
                    }
                }
            }
        }
    }

    private fun saveLink(link: String) {
        setDataToClipboard(link)
        viewState.onSnackBar(context.getString(R.string.rooms_info_copy_link_to_clipboard))
    }

    private fun getInternalLink(folder: CloudFolder): String {
        return "${context.accountOnline?.portal?.urlWithScheme}" + if (folder.isRoom) {
            "/rooms/shared/filter?folder=${folder.id}"
        } else {
            "rooms/shared/${folder.id}/filter?folder=${folder.id}"
        }
    }

    fun muteRoomNotifications(muted: Boolean) {
        presenterScope.launch {
            val roomId = roomClicked?.id.orEmpty()
            roomProvider?.muteRoomNotifications(roomId, muted)?.collect { result ->
                when (result) {
                    is Result.Error -> withContext(Dispatchers.Main) {
                        viewState.onError(context.getString(R.string.errors_unknown_error))
                    }
                    is Result.Success -> withContext(Dispatchers.Main) {
                        roomClicked?.mute = roomId in result.result
                        viewState.onSnackBar(
                            context.getString(
                                if (muted) {
                                    R.string.rooms_notifications_disabled
                                } else {
                                    R.string.rooms_notifications_enabled
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    fun exportIndex() {
        viewState.onDialogProgress(
            context.getString(R.string.dialogs_wait_title),
            false,
            TAG_DIALOG_CANCEL_SINGLE_OPERATIONS
        )
        presenterScope.launch {
            roomProvider?.exportIndex(roomClicked?.id.orEmpty())?.collect { result ->
                when (result) {
                    is Result.Error -> fetchError(result.exception)
                    is Result.Success -> {
                        val operation = result.result
                        val progress = operation.percentage
                        viewState.onDialogProgress(100, progress)
                        if (progress == 100 || operation.isCompleted) {
                            viewState.onDialogClose()
                            viewState.onRoomExportIndex(operation)
                        }
                    }
                }
            }
        }
    }

    fun authRoomViaLink(password: String, tag: String) {
        showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        requestJob = presenterScope.launch {
            val requestToken = roomClicked?.requestToken.orEmpty()
            roomProvider?.authRoomViaLink(requestToken, password)?.collect { result ->
                when (result) {
                    is Result.Error -> fetchError(result.exception)
                    is Result.Success -> {
                        val roomId = result.result
                        if (roomId == null) {
                            viewState.onRoomViaLinkPasswordRequired(true, tag)
                        } else {
                            roomClicked?.passwordProtected = false
                            refresh {
                                viewState.onDialogClose()
                                when (tag) {
                                    TAG_PROTECTED_ROOM_OPEN_FOLDER -> openFolder(result.result, 0)
                                    TAG_PROTECTED_ROOM_DOWNLOAD -> createDownloadFile()
                                    TAG_PROTECTED_ROOM_SHOW_INFO -> viewState.showRoomInfoFragment()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopFillingForm() {
        showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        requestJob = presenterScope.launch {
            (fileProvider as? CloudFileProvider ?: return@launch)
                .stopFilling(itemClicked?.id.orEmpty())
                .collect { result ->
                    viewState.onDialogClose()
                    when (result) {
                        is Result.Error -> fetchError(result.exception)
                        is Result.Success -> refresh()
                    }
                }
        }
    }
}