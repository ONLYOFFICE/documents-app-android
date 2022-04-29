package app.editors.manager.mvp.presenters.main

import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.documents.core.account.CloudAccount
import app.documents.core.account.Recent
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.managers.providers.CloudFileProvider
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.DownloadReceiver.OnDownloadListener
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.receivers.UploadReceiver.OnUploadListener
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.managers.works.UploadWork
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.models.request.RequestDeleteShare
import app.editors.manager.mvp.models.request.RequestExternal
import app.editors.manager.mvp.models.request.RequestFavorites
import app.editors.manager.mvp.models.response.ResponseExternal
import app.editors.manager.mvp.views.main.DocsCloudView
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import moxy.InjectViewState
import java.util.*

@InjectViewState
class DocsCloudPresenter(private val account: CloudAccount) : DocsBasePresenter<DocsCloudView>(),
    OnDownloadListener,
    OnUploadListener {

    private var externalAccessType: String? = null

    private val downloadReceiver: DownloadReceiver = DownloadReceiver()
    private val uploadReceiver: UploadReceiver = UploadReceiver()

    private var api: Api? = null

    private var currentSectionType = ApiContract.SectionType.UNKNOWN

    init {
        App.getApp().appComponent.inject(this)
        api = context.api()
        fileProvider = CloudFileProvider()
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
        downloadReceiver.setOnDownloadListener(null)
        uploadReceiver.setOnUploadListener(null)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(uploadReceiver)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadReceiver)
    }

    override fun onItemClick(item: Item, position: Int) {
        onClickEvent(item, position)
        isContextClick = false
        if (isSelectionMode) {
            val isChecked = !itemClicked!!.isSelected
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
                openFolder((itemClicked as CloudFolder).id, position)
            } else if (itemClicked is CloudFile) {
                getFileInfo()
            }
        } else {
            viewState.onSnackBarWithAction(
                context.getString(R.string.trash_snackbar_move_text),
                context.getString(R.string.trash_snackbar_move_button)
            ) { moveContext() }
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
                disposable.add(provider.getFiles(id, args).subscribe({ explorer: Explorer? ->
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
        if (preferenceTool.portal != null) {
            FirebaseUtils.addAnalyticsCreateEntity(
                preferenceTool.portal!!,
                true,
                StringUtils.getExtensionFromPath(title)
            )
        }
        val id = modelExplorerStack.currentId
        if (id != null) {
            val requestCreate = RequestCreate()
            requestCreate.title = title
            fileProvider?.let { provider ->
                disposable.add(
                    provider.createFile(id, requestCreate).subscribe({ file ->
                        addFile(file)
                        addRecent(file)
                        setPlaceholderType(PlaceholderViews.Type.NONE)
                        viewState.onDialogClose()
                        viewState.onCreateFile(file)
                    }) { throwable: Throwable -> fetchError(throwable) })
            }
            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        }
    }

    override fun getFileInfo() {
        if (itemClicked != null) {
            fileProvider?.let { provider ->
                disposable.add(provider.fileInfo(itemClicked!!)
                    .subscribe({ onFileClickAction() }) { throwable: Throwable -> fetchError(throwable) })
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
            viewState.onStateActionButton(isContextEditable)
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
                    viewState.onStateActionButton(isContextEditable)
                }
            }
            viewState.onStateAdapterRoot(true)
            viewState.onStateUpdateRoot(true)
        }
    }

    override fun onContextClick(item: Item, position: Int, isTrash: Boolean) {
        onClickEvent(item, position)
        isContextClick = true
        val state = ContextBottomDialog.State()
        state.title = itemClickedTitle
        state.info = TimeUtils.formatDate(itemClickedDate)
        state.isFolder = !isClickedItemFile
        state.isShared = isClickedItemShared
        state.isCanShare = isItemShareable
        state.isCanRename = isItemReadWrite
        state.isDocs = isClickedItemDocs
        state.isContextEditable = isContextItemEditable
        state.isItemEditable = isItemEditable
        state.isStorage = isClickedItemStorage && isRoot
        state.isDeleteShare = isShareSection
        state.isWebDav = false
        state.isOneDrive = false
        state.isGoogleDrive = false
        state.isDropBox = false
        state.isTrash = isTrash
        state.isFavorite = isClickedItemFavorite
        state.isPersonalAccount = account.isPersonal()
        if (!isClickedItemFile) {
            if ((itemClicked as CloudFolder).providerKey.isEmpty()) {
                state.iconResId = R.drawable.ic_type_folder
            } else {
                state.iconResId = StorageUtils.getStorageIcon((itemClicked as CloudFolder).providerKey)
            }
        } else {
            state.iconResId = getIconContext(
                StringUtils.getExtensionFromPath(
                    itemClickedTitle
                )
            )
        }
        state.isPdf = isPdf
        if (state.isShared && state.isFolder) {
            state.iconResId = R.drawable.ic_type_folder_shared
        }
        viewState.onItemContext(state)
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
    override fun onDownloadError(id: String?, url: String?, title: String, info: String, uri: Uri) {

        viewState.onDialogClose()
        viewState.onSnackBar(info)
    }

    override fun onDownloadProgress(id: String, total: Int, progress: Int) {
        viewState.onDialogProgress(total, progress)
    }

    override fun onDownloadComplete(
        id: String,
        url: String,
        title: String,
        info: String,
        path: String,
        mime: String,
        uri: Uri,
    ) {
        viewState.onFinishDownload(uri)
        viewState.onDialogClose()
        viewState.onSnackBarWithAction(
            """
    $info
    $title
    """.trimIndent(), context.getString(R.string.download_manager_open)
        ) { showDownloadFolderActivity(uri) }
    }

    override fun onDownloadCanceled(id: String, info: String) {
        viewState.onDialogClose()
        viewState.onSnackBar(info)
    }

    override fun onDownloadRepeat(id: String, title: String, info: String) {
        viewState.onDialogClose()
        viewState.onSnackBar(info)
    }

    override fun onUploadError(path: String?, info: String, file: String) {
        viewState.onSnackBar(info)
        viewState.onDeleteUploadFile(file)
    }

    override fun onUploadComplete(
        path: String,
        info: String,
        title: String?,
        file: CloudFile,
        id: String,
    ) {
        viewState.onSnackBar(info)
        if (modelExplorerStack.currentId == file.folderId) {
            addFile(file)
        }
        viewState.onDeleteUploadFile(id)
    }

    override fun onUploadAndOpen(path: String, title: String?, file: CloudFile, id: String) {
        viewState.onFileWebView(file)
    }

    override fun onUploadFileProgress(progress: Int, id: String, folderId: String) {
        if (modelExplorerStack.currentId == folderId) {
            viewState.onUploadFileProgress(progress, id)
        }
    }

    override fun onUploadCanceled(path: String, info: String, id: String) {
        viewState.onSnackBar(info)
        viewState.onDeleteUploadFile(id)
        if (UploadWork.getUploadFiles(modelExplorerStack.currentId)?.isEmpty() == true) {
            viewState.onRemoveUploadHead()
            getListWithHeaders(modelExplorerStack.last(), true)
        }
    }

    override fun onUploadRepeat(path: String, info: String) {
        viewState.onDialogClose()
        viewState.onSnackBar(info)
    }

    fun onEditContextClick() {
        if (itemClicked is CloudFile) {
            val file = itemClicked as CloudFile
            file.isReadOnly = false
            var url = file.webUrl
            if (url.contains(ApiContract.Parameters.ARG_ACTION) && url.contains(ApiContract.Parameters.VAL_ACTION_VIEW)) {
                url = url.substring(0, url.indexOf('&'))
                file.webUrl = url
            }
            addRecent(file)
            viewState.onFileWebView(file)
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

    val externalLink: Unit
        get() {
            if (itemClicked != null) {
                externalAccessType = ApiContract.ShareType.READ
                val requestExternal = RequestExternal()
                requestExternal.share = externalAccessType
                fileProvider?.let { provider ->
                    disposable.add(provider.share(itemClicked!!.id, requestExternal)!!
                        .subscribe({ responseExternal: ResponseExternal ->
                            itemClicked!!.shared = !itemClicked!!.shared
                            when (externalAccessType) {
                                ApiContract.ShareType.NONE -> viewState.onDocsAccess(
                                    false,
                                    context.getString(R.string.share_access_denied)
                                )
                                ApiContract.ShareType.READ, ApiContract.ShareType.READ_WRITE, ApiContract.ShareType.REVIEW -> {
                                    KeyboardUtils.setDataToClipboard(
                                        context,
                                        responseExternal.response,
                                        context.getString(R.string.share_clipboard_external_link_label)
                                    )
                                    viewState.onDocsAccess(
                                        true,
                                        context.getString(R.string.share_clipboard_external_copied)
                                    )
                                }
                            }
                        }) { throwable: Throwable -> fetchError(throwable) })
                }
            }
        }

    fun addToFavorite() {
        val requestFavorites = RequestFavorites()
        requestFavorites.fileIds = ArrayList(listOf(itemClicked!!.id))
        fileProvider?.let { provider ->
            disposable.add(provider.addToFavorites(requestFavorites)!!
                .subscribe({
                    (itemClicked as? CloudFile)?.fileStatus = ApiContract.FileStatus.FAVORITE.toString()
                    viewState.onUpdateItemFavorites()
                    viewState.onSnackBar(context.getString(R.string.operation_add_to_favorites))
                }) { throwable: Throwable -> fetchError(throwable) })
        }
    }

    fun deleteFromFavorite() {
        val requestFavorites = RequestFavorites()
        requestFavorites.fileIds = ArrayList(listOf(itemClicked!!.id))
        fileProvider?.let { provider ->
            disposable.add(provider.deleteFromFavorites(requestFavorites)!!
                .subscribe({
                    (itemClicked as? CloudFile)?.fileStatus = ApiContract.FileStatus.NONE.toString()
                    viewState.onUpdateItemFavorites()
                    viewState.onSnackBar(context.getString(R.string.operation_remove_from_favorites))
                }) { throwable: Throwable -> fetchError(throwable) })
        }
    }

    fun removeFromFavorites() {
        if (itemClicked?.id != null) {
            modelExplorerStack.removeItemById(itemClicked?.id)
            viewState.onDocsGet(getListWithHeaders(modelExplorerStack.last(), true))
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
                    setPlaceholderType(if (modelExplorerStack.isListEmpty)
                        PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
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

    private fun checkMoveCopyFiles(action: String) {
        val filesIds = operationStack?.selectedFilesIds
        val foldersIds = operationStack?.selectedFoldersIds

        disposable.add((fileProvider as CloudFileProvider).api.checkFiles(
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
            }, {
                fetchError(it)
            })
        )
    }

    private fun showMoveCopyDialog(files: List<CloudFile>, action: String, titleFolder: String?) {
        val names = ArrayList<String>()
        for (file in files) {
            names.add(file.title)
        }
        viewState.showMoveCopyDialog(names, action, titleFolder)
    }

    private fun onFileClickAction() {
        if (itemClicked is CloudFile) {
            val file = itemClicked as CloudFile
            val extension = file.fileExst
            when (StringUtils.getExtension(extension)) {
                StringUtils.Extension.DOC, StringUtils.Extension.SHEET, StringUtils.Extension.PRESENTATION, StringUtils.Extension.PDF, StringUtils.Extension.FORM -> {
                    addRecent(itemClicked as CloudFile)
                    //TODO open write mode
//                    file.isReadOnly = true
                    viewState.onFileWebView(file)
                }
                StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                    addRecent(itemClicked as CloudFile)
                    viewState.onFileMedia(getListMedia(file.id), false)
                }
                else -> viewState.onFileDownloadPermission()
            }
            FirebaseUtils.addAnalyticsOpenEntity(networkSettings.getPortal(), extension)
        }
    }

    fun openFile(data: String) {
        val model = Json.decodeFromString<OpenDataModel>(data)
        fileProvider?.let { provider ->
            disposable.add(provider.fileInfo(CloudFile().apply {
                id = model.file?.id.toString()
            }).subscribe({ file: CloudFile ->
                itemClicked = file
                when (StringUtils.getExtension(file.fileExst)) {
                    StringUtils.Extension.DOC, StringUtils.Extension.SHEET, StringUtils.Extension.PRESENTATION, StringUtils.Extension.PDF, StringUtils.Extension.FORM -> {
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
        get() = isUserSection || isCommonSection && (isAdmin || isContextReadWrite && !isRoot) ||
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

    private val isClickedItemShared: Boolean
        get() = itemClicked?.shared == true

    private val isClickedItemFavorite: Boolean
        get() = itemClicked?.favorite == true

    private val isItemOwner: Boolean
        get() = StringUtils.equals(itemClicked?.createdBy?.id, account.id)

    private val isItemReadWrite: Boolean
        get() = itemClicked?.intAccess == ApiContract.ShareCode.READ_WRITE ||
                itemClicked?.intAccess == ApiContract.ShareCode.NONE

    private val isItemEditable: Boolean
        get() = !isVisitor && !isProjectsSection && (isItemOwner || isItemReadWrite ||
                itemClicked?.intAccess == ApiContract.ShareCode.REVIEW ||
                itemClicked?.intAccess == ApiContract.ShareCode.FILL_FORMS ||
                itemClicked?.intAccess == ApiContract.ShareCode.COMMENT)

    private val isItemShareable: Boolean
        get() = isItemEditable && (!isCommonSection || isAdmin) && !isProjectsSection
                && !isBunchSection && isItemReadWrite

    private val isClickedItemStorage: Boolean
        get() = itemClicked?.providerItem == true


    private fun showDownloadFolderActivity(uri: Uri) {
        viewState.onDownloadActivity(uri)
    }

    fun setSectionType(sectionType: Int) {
        currentSectionType = sectionType
    }
}