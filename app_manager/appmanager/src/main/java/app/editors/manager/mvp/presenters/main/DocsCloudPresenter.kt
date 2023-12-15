package app.editors.manager.mvp.presenters.main

import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import app.documents.core.network.share.models.request.Invitation
import app.documents.core.network.share.models.request.RequestRoomShare
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.RoomProvider
import app.documents.core.storage.account.CloudAccount
import app.documents.core.storage.recent.Recent
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.DownloadReceiver.OnDownloadListener
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.receivers.UploadReceiver.OnUploadListener
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.works.UploadWork
import app.editors.manager.mvp.models.filter.Filter
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.views.main.DocsCloudView
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.presenterScope
import java.util.Date

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
                    return isRoom && modelExplorerStack.rootId == id
                }

                override fun isArchive(): Boolean {
                    return currentSectionType == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM
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
        if (super.copy()) {
            checkMoveCopyFiles(MoveCopyDialog.ACTION_COPY)
            return true
        }
        return false
    }

    override fun move(): Boolean {
        return if (super.move()) {
            checkMoveCopyFiles(MoveCopyDialog.ACTION_MOVE)
            true
        } else {
            false
        }
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
            networkSettings.getPortal(),
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
                                viewState.onCreateFile(info[0] as CloudFile)
                            }
                        }
                    }) { throwable: Throwable -> fetchError(throwable) })
            }
            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        }
    }

    override fun getFileInfo() {
        val item = itemClicked
        if (item != null) {
            fileProvider?.let { provider ->
                disposable.add(
                    provider.fileInfo(item)
                        .doOnSubscribe { showDialogWaiting(TAG_DIALOG_CLEAR_DISPOSABLE) }
                        .doOnError(::fetchError)
                        .subscribe(::onFileClickAction)
                )
            }
        }
    }

    override fun addRecent(file: CloudFile) {
        CoroutineScope(Dispatchers.Default).launch {
            recentDao.addRecent(
                Recent(
                    idFile = file.id,
                    path = null,
                    name = file.title,
                    size = file.pureContentLength,
                    isLocal = false,
                    isWebDav = account.isWebDav,
                    date = Date().time,
                    ownerId = account.id,
                    source = account.portal
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
                viewState.onStateActionButton(isContextEditable)
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
            !isVisitor
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
        } else if (isRoom && isRoot) {
            resetFilters()
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
        val file = itemClicked
        if (file is CloudFile) {
            if (LocalContentTools.isOpenFormat(file.clearExt)) {
                viewState.onConversionQuestion()
                return
            }
            file.isReadOnly = false
            var url = file.webUrl
            if (url.contains(ApiContract.Parameters.ARG_ACTION) && url.contains(ApiContract.Parameters.VAL_ACTION_VIEW)) {
                url = url.substring(0, url.indexOf('&'))
                file.webUrl = url
            }
            addRecent(file)
            onFileClickAction(file, true)
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
            StringUtils.Extension.FORM -> {
                disposable.add(
                    (fileProvider as CloudFileProvider).opeEdit(cloudFile).subscribe({ info ->
                        checkSdkVersion { result ->
                            viewState.onDialogClose()
                            if (result) {
                                viewState.onOpenDocumentServer(cloudFile, info, isEdit)
                            } else {
                                viewState.onFileWebView(cloudFile, true)
                            }
                        }
                    }) { error ->
                        fetchError(error)
                    }
                )
                addRecent(itemClicked as CloudFile)
            }

            StringUtils.Extension.PDF -> {
                viewState.onFileWebView(cloudFile, true)
            }

            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                addRecent(itemClicked as CloudFile)
                viewState.onFileMedia(getListMedia(cloudFile.id), false)
            }

            else -> viewState.onFileDownloadPermission()
        }
        FirebaseUtils.addAnalyticsOpenEntity(networkSettings.getPortal(), extension)
    }

    private fun resetFilters() {
        preferenceTool.filter = Filter()
        viewState.onStateUpdateFilterMenu()
    }

    @Suppress("KotlinConstantConditions")
    fun openFile(data: String) {
        val model = Json.decodeFromString<OpenDataModel>(data)
        if (model.file?.id == null && model.folder?.id != null) {
            openFolder(model.folder.id.toString(), 0)
            return
        }
        fileProvider?.let { provider ->
            disposable.add(provider.fileInfo(CloudFile().apply {
                id = model.file?.id.toString()
            })
                .flatMap { cloudFile ->
                    (provider as CloudFileProvider).opeEdit(cloudFile)
                        .toObservable()
                        .zipWith(Observable.fromCallable { cloudFile }) { info, file ->
                            return@zipWith arrayOf(file, info)
                        }
                }
                .subscribe({ info ->
                    val file = info[0] as CloudFile
                    when (val ext = StringUtils.getExtension(file.fileExst)) {
                        StringUtils.Extension.DOC, StringUtils.Extension.SHEET, StringUtils.Extension.PRESENTATION, StringUtils.Extension.FORM -> {
                            if (BuildConfig.APPLICATION_ID != "com.onlyoffice.documents" && ext == StringUtils.Extension.FORM) {
                                viewState.onError(context.getString(R.string.error_unsupported_format))
                            } else {
                                viewState.onOpenDocumentServer(file, info[1] as String, false)
                            }
                        }

                        StringUtils.Extension.PDF -> {
                            viewState.onFileWebView(file)
                        }

                        StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                            viewState.onFileMedia(getListMedia(file.id), false)
                        }

                        else -> viewState.onFileDownloadPermission()
                    }
                }
                ) { throwable: Throwable ->
                    fetchError(throwable)
                })
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
        get() = if (networkSettings.isDocSpace && currentSectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM) {
            itemClicked?.isCanEdit == true
        } else {
            !isVisitor && !isProjectsSection && (isItemOwner || isItemReadWrite ||
                    itemClicked?.intAccess == ApiContract.ShareCode.REVIEW ||
                    itemClicked?.intAccess == ApiContract.ShareCode.FILL_FORMS ||
                    itemClicked?.intAccess == ApiContract.ShareCode.COMMENT)
        }

    private val isItemShareable: Boolean
        get() = if (networkSettings.isDocSpace && currentSectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM) {
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
                    }.flatMap { id-> it.archiveRoom(id, isArchive) }
                        .doOnSubscribe { viewState.onSwipeEnable(true) }
                        .lastElement()
                        .subscribe({ response ->
                            if (response.statusCode.toInt() == ApiContract.HttpCodes.SUCCESS) {
                                viewState.onSwipeEnable(false)
                                setSelection(false)
                                refresh()
                            }
                        }, ::fetchError)
                } else {
                    it.archiveRoom(itemClicked?.id ?: "", isArchive = isArchive)
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

    fun editRoom() {
        if (itemClicked is CloudFolder && (itemClicked as CloudFolder).isRoom) {
            viewState.onCreateRoom((itemClicked as CloudFolder).roomType, itemClicked as CloudFolder)
        }
    }

    fun copyGeneralLink() {

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

    fun createRoomFromFolder() {
        if (itemClicked is CloudFolder) {
            viewState.onCreateRoom(cloudFolder = itemClicked as CloudFolder, isCopy = true)
        }
    }

    fun deleteRoom() {
        if (isSelectionMode && modelExplorerStack.countSelectedItems > 0) {
            val ids = modelExplorerStack.selectedFolders.map {
                it.id
            }
            roomProvider?.let { provider ->
                disposable.add(
                    provider.deleteRoom(items = ids).subscribe({
                        viewState.onDialogClose()
                        viewState.onSnackBar(context.getString(R.string.room_delete_success))
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
            }
        }
    }

    fun checkRoomOwner() {
        if (itemClicked is CloudFolder) {
            viewState.onLeaveRoomDialog(
                R.string.leave_room_title,
                if (isItemOwner) R.string.leave_room_owner_desc else R.string.leave_room_desc,
                "leave",
                isItemOwner
            )
        }
    }

    fun leaveRoom() {
        if (!isItemOwner) {
            showDialogWaiting(null)
            presenterScope.launch {
                request(
                    func = {
                        context.shareApi.shareRoom(
                            itemClicked?.id ?: "", RequestRoomShare(
                                invitations = listOf(Invitation(id = account.id, access = ApiContract.Access.None.code))
                            )
                        )
                    },
                    onSuccess = {
                        viewState.onDialogClose()
                        viewState.onSnackBar(context.getString(R.string.leave_room_message))
                        refresh()
                    },
                    onError = {
                        viewState.onDialogClose()
                        fetchError(it)
                    }
                )
            }
        } else {
            viewState.showSetOwnerFragment(itemClicked as CloudFolder)
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
}