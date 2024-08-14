package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.Recent
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.extensions.request
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestDeleteShare
import app.documents.core.network.manager.models.request.RequestFavorites
import app.documents.core.network.share.models.request.RequestRoomShare
import app.documents.core.network.share.models.request.UserIdInvitation
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.RoomProvider
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
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.receivers.UploadReceiver.OnUploadListener
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.works.UploadWork
import app.editors.manager.mvp.models.filter.Filter
import app.editors.manager.mvp.models.list.RecentViaLink
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.views.main.DocsCloudView
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.viewModels.main.CopyItems
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.presenterScope
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@InjectViewState
class DocsCloudPresenter(private val account: CloudAccount) : DocsBasePresenter<DocsCloudView>(),
    OnDownloadListener,
    OnUploadListener {

    private val downloadReceiver: DownloadReceiver = DownloadReceiver()
    private val uploadReceiver: UploadReceiver = UploadReceiver()

    private var api: ManagerService? = null
    private var roomProvider: RoomProvider? = null

    private var conversionJob: Job? = null

    init {
        App.getApp().appComponent.inject(this)
        api = context.api
        roomProvider = context.roomProvider
        fileProvider = context.cloudFileProvider.apply {
            roomCallback = object : CloudFileProvider.RoomCallback {

                override fun isRoomRoot(id: String?): Boolean {
                    val parts = modelExplorerStack.last()?.pathParts.orEmpty()
                    return if (parts.isNotEmpty()) isRoom && parts[0].id == id else false
                }

                override fun isArchive(): Boolean = ApiContract.SectionType.isArchive(currentSectionType)

                override fun isRecent(): Boolean {
                    return modelExplorerStack.rootFolderType == ApiContract.SectionType.CLOUD_RECENT
                }
            }
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        downloadReceiver.setOnDownloadListener(this)
        uploadReceiver.setOnUploadListener(this)
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(uploadReceiver, uploadReceiver.filter)
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(downloadReceiver, downloadReceiver.filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        interruptConversion()
        downloadReceiver.setOnDownloadListener(null)
        uploadReceiver.setOnUploadListener(null)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(uploadReceiver)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadReceiver)
    }

    override fun onItemClick(item: Item, position: Int) {
        onClickEvent(item, position)
        itemClicked?.let { itemClicked ->
            if (isSelectionMode) {
                val isChecked = !itemClicked.isSelected
                modelExplorerStack.setSelectById(item, isChecked)
                if (!isSelectedItemsEmpty) {
                    viewState.onStateUpdateSelection(true)
                    viewState.onItemSelected(
                        position,
                        modelExplorerStack.countSelectedItems.toString()
                    )
                }
            } else if (!isTrashMode) {
                if (itemClicked is CloudFolder) {
                    openFolder(itemClicked.id, position)
                } else if (itemClicked is CloudFile) {
                    if (LocalContentTools.isOpenFormat(itemClicked.clearExt)) {
                        viewState.onConversionQuestion()
                    } else {
                        getFileInfo()
                    }
                } else if (itemClicked is RecentViaLink) {
                    openRecentViaLink()
                }
            } else {
                viewState.onSnackBarWithAction(
                    context.getString(R.string.trash_snackbar_move_text),
                    context.getString(R.string.trash_snackbar_move_button)
                ) { moveCopySelected(OperationsState.OperationType.RESTORE) }
            }
        }
    }

    override fun copy(): Boolean {
        if (!checkFillFormsRoom()) {
            return false
        }

        if (super.copy()) {
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
            fileProvider?.let { provider ->
                disposable.add(provider.getFiles(id, args.putFilters()).subscribe({ explorer: Explorer? ->
                    modelExplorerStack.addOnNext(explorer)
                    val last = modelExplorerStack.last()
                    if (last != null) {
                        viewState.onDocsNext(getListWithHeaders(last, true))
                    }
                }) { throwable: Throwable -> fetchError(throwable) })
            }
        }
    }

    override fun createDocs(title: String) {
        FirebaseUtils.addAnalyticsCreateEntity(
            account.portalUrl,
            true,
            StringUtils.getExtensionFromPath(title)
        )

        modelExplorerStack.currentId?.let { id ->
            val requestCreate = RequestCreate()
            requestCreate.title = title
            fileProvider?.let { provider ->
                disposable.add(
                    provider.createFile(id, requestCreate).flatMap { cloudFile ->
                        addFile(cloudFile)
                        addRecent(cloudFile)
                        (provider as CloudFileProvider).opeEdit(cloudFile)
                            .toObservable()
                            .zipWith(Observable.fromCallable { cloudFile }) { info, file ->
                                return@zipWith arrayOf(file, info)
                            }
                    }.subscribe({ info ->
                        setPlaceholderType(PlaceholderViews.Type.NONE)
                        viewState.onDialogClose()
                        checkSdkVersion { result ->
                            if (result) {
                                viewState.onOpenDocumentServer(info[0] as CloudFile, info[1] as String, true)
                            } else {
                                downloadTempFile(info[0] as CloudFile, true)

                            }
                        }
                    }) { throwable: Throwable -> fetchError(throwable) })
            }
            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        }
    }

    override fun getFileInfo() {
        val item = itemClicked
        if (item != null && item is CloudFile) {
            fileProvider?.let { provider ->
                disposable.add(
                    provider.fileInfo(item)
                        .doOnSubscribe { showDialogWaiting(TAG_DIALOG_CLEAR_DISPOSABLE) }
                        .subscribe(::onFileClickAction) { onFileClickAction(item) }
                )
            }
        }
    }

    override fun addRecent(file: CloudFile) {
        presenterScope.launch {
            recentDataSource.add(
                Recent(
                    fileId = file.id,
                    path = "",
                    name = file.title,
                    size = file.pureContentLength,
                    ownerId = account.id,
                    source = account.portalUrl
                )
            )
        }
    }

    override fun updateViewsState() {
        if (isSelectionMode) {
            viewState.onStateUpdateSelection(true)
            viewState.onActionBarTitle(modelExplorerStack.countSelectedItems.toString())
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
            if (isRoom && modelExplorerStack.last()?.current?.security?.create == true) {
                viewState.onStateActionButton(true)
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

                isFoldersMode -> {
                    viewState.onActionBarTitle(context.getString(R.string.operation_title))
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
    }

    override fun onActionClick() {
        viewState.onActionDialog(
            isRoot && (isUserSection || isCommonSection && isAdmin),
            !isVisitor,
            modelExplorerStack.last()?.current?.roomType
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
        viewState.onDeleteUploadFile(file)
        viewState.onSnackBar(info)
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
        viewState.onDeleteUploadFile(id)
        viewState.onSnackBar(info)
    }

    override fun onUploadAndOpen(path: String?, title: String?, file: CloudFile?, id: String?) {
        viewState.onFileWebView(checkNotNull(file))
    }

    override fun onUploadFileProgress(progress: Int, id: String?, folderId: String?) {
        if (folderId != null && id != null && modelExplorerStack.currentId == folderId) {
            viewState.onUploadFileProgress(progress, id)
        }
    }

    override fun onUploadCanceled(path: String?, info: String?, id: String?) {
        viewState.onSnackBar(info)
        viewState.onDeleteUploadFile(id)
        if (UploadWork.getUploadFiles(modelExplorerStack.currentId)?.isEmpty() == true) {
            viewState.onRemoveUploadHead()
            getListWithHeaders(modelExplorerStack.last(), true)
        }
    }

    override fun onUploadRepeat(path: String?, info: String?) {
        viewState.onDialogClose()
        viewState.onSnackBar(info)
    }

    override fun getBackStack(): Boolean {
        val backStackResult = super.getBackStack()
        if (modelExplorerStack.last()?.filterType != preferenceTool.filter.type.filterVal) {
            refresh()
        }
        return backStackResult
    }

    override fun openFolder(id: String?, position: Int) {
        setFiltering(false)
        resetFilters()
        super.openFolder(id, position)
    }

    fun onEditContextClick() {
        when (val item = itemClicked) {
            is CloudFile -> {
                if (LocalContentTools.isOpenFormat(item.clearExt)) {
                    viewState.onConversionQuestion()
                    return
                }
                item.isReadOnly = false
                var url = item.webUrl
                if (url.contains(ApiContract.Parameters.ARG_ACTION) && url.contains(ApiContract.Parameters.VAL_ACTION_VIEW)) {
                    url = url.substring(0, url.indexOf('&'))
                    item.webUrl = url
                }
                addRecent(item)
                onFileClickAction(item, true)
            }
            is CloudFolder -> editRoom()
        }
    }

    fun removeShareSelected() {
        if (modelExplorerStack.countSelectedItems > 0) {
            val deleteShare = RequestDeleteShare()
            deleteShare.folderIds = modelExplorerStack.selectedFoldersIds
            deleteShare.fileIds = modelExplorerStack.selectedFilesIds
            disposable.add(Observable
                .fromCallable { api?.deleteShare(deleteShare)?.execute() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    modelExplorerStack.removeSelected()
                    resetDatesHeaders()
                    setPlaceholderType(if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                    viewState.onActionBarTitle("0")
                    viewState.onDeleteBatch(getListWithHeaders(modelExplorerStack.last(), true))
                    onBatchOperations()
                }) { throwable: Throwable -> fetchError(throwable) })
        }

    }

    fun removeShare() {
        if (modelExplorerStack.countSelectedItems > 0) {
            viewState.onDialogQuestion(
                context.getString(R.string.dialogs_question_share_remove), null,
                TAG_DIALOG_ACTION_REMOVE_SHARE
            )
        } else {
            viewState.onSnackBar(context.getString(R.string.operation_empty_lists_data))
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
                            setDataToClipboard(externalLink)
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
        (fileProvider as CloudFileProvider).let { provider ->
            val isAdd = itemClicked?.favorite?.not() == true

            disposable.add(provider.addToFavorites(requestFavorites, isAdd)
                .subscribe({
                    (itemClicked as? CloudFile)?.fileStatus = if (isAdd) {
                        ApiContract.FileStatus.FAVORITE.toString()
                    } else {
                        ApiContract.FileStatus.NONE.toString()
                    }
                    viewState.onUpdateFavoriteItem()
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
            val provider = fileProvider as CloudFileProvider
            showDialogProgress(true, TAG_DIALOG_CANCEL_BATCH_OPERATIONS)
            batchDisposable = provider.clearTrash()
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

    private fun setDataToClipboard(value: String) {
        KeyboardUtils.setDataToClipboard(
            context,
            value,
            context.getString(R.string.share_clipboard_external_link_label)
        )
        viewState.onSnackBar(context.getString(R.string.share_clipboard_external_copied))
    }

    private fun checkMoveCopyFiles(action: String) {
        val filesIds = operationStack?.selectedFilesIds
        val foldersIds = operationStack?.selectedFoldersIds

        api?.let { api ->
            disposable.add(api.checkFiles(
                destFolderId ?: "",
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

    private fun onFileClickAction(cloudFile: CloudFile, isEdit: Boolean = false) {
        val extension = cloudFile.fileExst
        when (StringUtils.getExtension(extension)) {
            StringUtils.Extension.DOC,
            StringUtils.Extension.SHEET,
            StringUtils.Extension.PRESENTATION,
            StringUtils.Extension.FORM,
            StringUtils.Extension.PDF -> {
                checkSdkVersion { result ->
                    if (result) {
                        openDocumentServer(cloudFile, isEdit)
                    } else {
                        downloadTempFile(cloudFile, isEdit)
                    }
                }
            }

            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                addRecent(itemClicked as CloudFile)
                viewState.onFileMedia(getListMedia(cloudFile.id), false)
            }

            else -> viewState.onFileDownloadPermission()
        }
        FirebaseUtils.addAnalyticsOpenEntity(account.portalUrl, extension)
    }

    private fun openDocumentServer(cloudFile: CloudFile, isEdit: Boolean) {
        with(fileProvider as CloudFileProvider) {
            val token = AccountUtils.getToken(context, account.accountName)
            disposable.add(
                openDocument(cloudFile, token).subscribe({ result ->
                    viewState.onDialogClose()
                    if (result.isPdf) {
                        downloadTempFile(cloudFile, false)
                    } else if (result.info != null) {
                        viewState.onOpenDocumentServer(cloudFile, result.info, isEdit)
                    }
                }) { error ->
                    fetchError(error)
                }
            )
        }
        addRecent(itemClicked as CloudFile)
    }

    private fun resetFilters() {
        preferenceTool.filter = Filter()
        viewState.onStateUpdateFilterMenu()
    }

    fun openFile(data: String) {
        val model = Json.decodeFromString<OpenDataModel>(data)
        if (model.file?.id == null && model.folder?.id != null) {
            openFolder(model.folder.id.toString(), 0)
            return
        }
        fileProvider?.let { provider ->
            disposable.add(provider.fileInfo(CloudFile().apply {
                id = model.file?.id.toString()
            }).subscribe({ cloudFile ->
                itemClicked = cloudFile
                onFileClickAction(cloudFile)
            }, { error ->
                fetchError(error)
            }))
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

    private val isContextOwner: Boolean
        get() = StringUtils.equals(modelExplorerStack.currentFolderOwnerId, account.id)

    private val isContextReadWrite: Boolean
        get() = isContextOwner || modelExplorerStack.currentFolderAccess == ApiContract.ShareCode.READ_WRITE ||
                modelExplorerStack.currentFolderAccess == ApiContract.ShareCode.NONE

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

    private val isClickedItemShared: Boolean
        get() = itemClicked?.shared == true

    private val isClickedItemFavorite: Boolean
        get() = itemClicked?.favorite == true

    private val isItemOwner: Boolean
        get() = StringUtils.equals(itemClicked?.createdBy?.id, account.id)

    private val isItemReadWrite: Boolean
        get() = itemClicked?.intAccess == ApiContract.ShareCode.READ_WRITE || isUserSection

    private val isItemEditable: Boolean
        get() = if (account.isDocSpace && currentSectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM) {
            itemClicked?.isCanEdit == true
        } else {
            !isVisitor && !isProjectsSection && (isItemOwner || isItemReadWrite ||
                    itemClicked?.intAccess == ApiContract.ShareCode.REVIEW ||
                    itemClicked?.intAccess == ApiContract.ShareCode.FILL_FORMS ||
                    itemClicked?.intAccess == ApiContract.ShareCode.COMMENT)
        }

    private val isItemShareable: Boolean
        get() = if (account.isDocSpace && currentSectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM) {
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

    fun archiveRoom(isArchive: Boolean = true) {
        roomProvider?.let {
            disposable.add(
                if (isSelectionMode) {
                    Observable.fromArray(modelExplorerStack.selectedFolders).flatMapIterable { room ->
                        room.map { item -> item.id }
                    }.flatMap { id -> it.archiveRoom(id, isArchive) }
                        .doOnSubscribe { viewState.onSwipeEnable(true) }
                        .lastElement()
                        .subscribe({ response ->
                            if (response.statusCode.toInt() == ApiContract.HttpCodes.SUCCESS) {
                                viewState.onArchiveRoom(isArchive, modelExplorerStack.selectedFolders.size)
                                viewState.onSwipeEnable(false)
                                setSelection(false)
                                refresh()
                            }
                        }, ::fetchError)
                } else {
                    it.archiveRoom(roomClicked?.id ?: "", isArchive = isArchive)
                        .doOnSubscribe { viewState.onSwipeEnable(true) }
                        .subscribe({ response ->
                            if (response.statusCode.toInt() == ApiContract.HttpCodes.SUCCESS) {
                                viewState.onArchiveRoom(isArchive)
                                viewState.onSwipeEnable(false)
                            }
                        }, ::fetchError)
                }
            )
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
        when  {
            (item as? CloudFolder)?.isRoom == true -> copyRoomLink()
            item is CloudFolder -> saveLink(getInternalLink(item))
            else -> saveExternalLinkToClipboard()
        }
    }

    fun archiveSelectedRooms() {
        roomProvider?.let { provider ->
            disposable.add(
                Observable
                    .zip(modelExplorerStack.selectedFoldersIds.map(provider::archiveRoom)) {}
                    .doOnSubscribe { viewState.onSwipeEnable(true) }
                    .subscribe {
                        viewState.onArchiveSelectedRooms(modelExplorerStack.selectedFolders)
                        viewState.onSwipeEnable(false)
                        deselectAll()
                    }
            )
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
                                    viewState.onUpdateFavoriteItem()
                                }
                            }, ::fetchError)
                    )
                }
            }
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
                    provider.deleteRoom(itemClicked?.id ?: "").subscribe({
                        viewState.onDialogClose()
                        viewState.onSnackBar(context.getString(R.string.room_delete_success))
                        refresh()
                    }) { fetchError(it) }
                )
            }

        }
    }

    fun interruptConversion(): Boolean {
        val cancelled = conversionJob?.isCancelled == true // conversionJob == null || isCancelled
        conversionJob?.cancel()
        return cancelled
    }

    fun convertToOOXML() {
        val extension = LocalContentTools.toOOXML((itemClicked as? CloudFile)?.clearExt.orEmpty())
        viewState.onConversionProgress(0, extension)
        (fileProvider as? CloudFileProvider)?.let { fileProvider ->
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
                                    access = ApiContract.Access.None.code
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

    @SuppressLint("MissingPermission")
    fun updateDocument(data: Uri) {
        if (data.path?.isEmpty() == true) return
        context.contentResolver.openInputStream(data).use {
            val file = File(data.path)
            val body = MultipartBody.Part.createFormData(
                file.name, file.name, RequestBody.create(
                    MediaType.parse(ContentResolverUtils.getMimeType(context, data)), file
                )
            )
            disposable.add(
                (fileProvider as CloudFileProvider).updateDocument(itemClicked?.id.orEmpty(), body)
                    .subscribe({ result ->
                        FileUtils.deletePath(file)
                        viewState.onDialogClose()
                    }, {
                        FileUtils.deletePath(file)
                        fetchError(it)
                    })
            )
        }

    }

    private fun openRecentViaLink() {
        setPlaceholderType(PlaceholderViews.Type.LOAD)
        (fileProvider as? CloudFileProvider)?.let { provider ->
            disposable.add(
                provider.getRecentViaLink()
                    .subscribe(::loadSuccess, ::fetchError)
            )
        }
    }

    private fun copyRoomLink() {
        roomClicked?.let { room ->
            if (room.roomType == ApiContract.RoomType.COLLABORATION_ROOM) {
                setDataToClipboard(getInternalLink(room))
            } else {
                presenterScope.launch {
                    val externalLink = roomProvider?.getExternalLink(roomClicked?.id.orEmpty())
                    withContext(Dispatchers.Main) {
                        if (externalLink.isNullOrEmpty()) {
                            viewState.onError(context.getString(R.string.errors_unknown_error))
                        } else {
                            saveLink(externalLink)
                        }
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
            "rooms/shared/filter?folder=${folder.id}"
        } else {
            "rooms/shared/${folder.id}/filter?folder=${folder.id}"
        }
    }

}