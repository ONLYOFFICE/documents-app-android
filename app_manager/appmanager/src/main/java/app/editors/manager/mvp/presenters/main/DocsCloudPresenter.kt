package app.editors.manager.mvp.presenters.main

import android.net.Uri
import android.view.View
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
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelExplorerStack
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
import io.reactivex.disposables.Disposable
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

    private val mGetDisposable = HashMap<String, Disposable>()
    private var mExternalAccessType: String? = null

    private val downloadReceiver: DownloadReceiver
    private val uploadReceiver: UploadReceiver

    private var api: Api? = null

    private var currentSectionType = ApiContract.SectionType.UNKNOWN

    init {
        App.getApp().appComponent.inject(this)
        api = mContext.api()
        downloadReceiver = DownloadReceiver()
        uploadReceiver = UploadReceiver()
        mModelExplorerStack = ModelExplorerStack()
        mFilteringValue = ""
        mPlaceholderType = PlaceholderViews.Type.NONE
        mIsContextClick = false
        mIsFilteringMode = false
        mIsSelectionMode = false
        mIsFoldersMode = false
        mIsTrashMode = false
        mFileProvider = CloudFileProvider()
    }


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        downloadReceiver.setOnDownloadListener(this)
        uploadReceiver.setOnUploadListener(this)
        LocalBroadcastManager.getInstance(mContext)
            .registerReceiver(uploadReceiver, uploadReceiver.filter)
        LocalBroadcastManager.getInstance(mContext)
            .registerReceiver(downloadReceiver, downloadReceiver.filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadReceiver.setOnDownloadListener(null)
        uploadReceiver.setOnUploadListener(null)
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(uploadReceiver)
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(downloadReceiver)
    }

    override fun onItemClick(item: Item, position: Int) {
        onClickEvent(item, position)
        mIsContextClick = false
        if (mIsSelectionMode) {
            val isChecked = !mItemClicked!!.isSelected
            mModelExplorerStack.setSelectById(item, isChecked)
            if (!isSelectedItemsEmpty) {
                viewState.onStateUpdateSelection(true)
                viewState.onItemSelected(
                    position,
                    mModelExplorerStack.countSelectedItems.toString()
                )
            }
        } else if (!mIsTrashMode) {
            if (mItemClicked is CloudFolder) {
                openFolder((mItemClicked as CloudFolder).id, position)
            } else if (mItemClicked is CloudFile) {
                getFileInfo()
            }
        } else {
            viewState.onSnackBarWithAction(
                mContext.getString(R.string.trash_snackbar_move_text),
                mContext.getString(R.string.trash_snackbar_move_button)
            ) { v: View? -> moveContext() }
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
        val id = mModelExplorerStack.currentId
        val loadPosition = mModelExplorerStack.loadPosition
        if (id != null && loadPosition > 0) {
            val args = getArgs(mFilteringValue)
            args[ApiContract.Parameters.ARG_START_INDEX] = loadPosition.toString()
            mDisposable.add(mFileProvider.getFiles(id, args).subscribe({ explorer: Explorer? ->
                mModelExplorerStack.addOnNext(explorer)
                val last = mModelExplorerStack.last()
                if (last != null) {
                    viewState.onDocsNext(getListWithHeaders(last, true))
                }
            }) { throwable: Throwable? -> fetchError(throwable) })
        }
    }

    override fun createDocs(title: String) {
        if (mPreferenceTool.portal != null) {
            FirebaseUtils.addAnalyticsCreateEntity(
                mPreferenceTool.portal!!,
                true,
                StringUtils.getExtensionFromPath(title)
            )
        }
        val id = mModelExplorerStack.currentId
        if (id != null) {
            val requestCreate = RequestCreate()
            requestCreate.title = title
            mDisposable.add(
                mFileProvider.createFile(id, requestCreate).subscribe({ file: CloudFile? ->
                    addFile(file)
                    setPlaceholderType(PlaceholderViews.Type.NONE)
                    viewState.onDialogClose()
                    viewState.onCreateFile(file)
                }) { throwable: Throwable? -> fetchError(throwable) })
            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        }
    }

    override fun getFileInfo() {
        if (mItemClicked != null) {
            mDisposable.add(mFileProvider.fileInfo(mItemClicked)
                .subscribe({ onFileClickAction() }) { throwable: Throwable? ->
                    fetchError(
                        throwable
                    )
                })
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
                    ownerId = account.id
                )
            )
        }
    }

    override fun updateViewsState() {
        if (mIsSelectionMode) {
            viewState.onStateUpdateSelection(true)
            viewState.onActionBarTitle(mModelExplorerStack.countSelectedItems.toString())
            viewState.onStateAdapterRoot(mModelExplorerStack.isNavigationRoot)
            viewState.onStateActionButton(false)
        } else if (mIsFilteringMode) {
            viewState.onActionBarTitle(mContext.getString(R.string.toolbar_menu_search_result))
            viewState.onStateUpdateFilter(true, mFilteringValue)
            viewState.onStateAdapterRoot(mModelExplorerStack.isNavigationRoot)
            viewState.onStateActionButton(false)
        } else if (!mModelExplorerStack.isRoot) {
            viewState.onStateAdapterRoot(false)
            viewState.onStateUpdateRoot(false)
            viewState.onStateActionButton(isContextEditable)
            viewState.onActionBarTitle(currentTitle)
        } else {
            when {
                mIsTrashMode -> {
                    viewState.onStateActionButton(false)
                    viewState.onActionBarTitle("")
                }
                mIsFoldersMode -> {
                    viewState.onActionBarTitle(mContext.getString(R.string.operation_title))
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
        mIsContextClick = true
        val state = ContextBottomDialog.State()
        state.title = itemClickedTitle
        state.info = TimeUtils.formatDate(itemClickedDate)
        state.isFolder = !isClickedItemFile
        state.isShared = isClickedItemShared
        state.isCanShare = isItemShareable
        state.isDocs = isClickedItemDocs
        state.isContextEditable = isContextItemEditable
        state.isItemEditable = isItemEditable
        state.isStorage = isClickedItemStorage && isRoot
        state.isDeleteShare = isShareSection
        state.isWebDav = false
        state.isOneDrive = false
        state.isTrash = isTrash
        state.isFavorite = isClickedItemFavorite
        if (!isClickedItemFile) {
            if((itemClicked as CloudFolder).providerKey.isEmpty()) {
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
    override fun onDownloadError(id: String?, url: String?, title: String, info: String) {
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
        uri: Uri
    ) {
        viewState.onDialogClose()
        viewState.onSnackBarWithAction(
            """
    $info
    $title
    """.trimIndent(), mContext.getString(R.string.download_manager_open)
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
        //getViewState().onDeleteUploadFile(file);
    }

    override fun onUploadComplete(
        path: String,
        info: String,
        title: String?,
        file: CloudFile,
        id: String
    ) {
        viewState.onSnackBar(info)
        if ( id != null && mModelExplorerStack.currentId == file.folderId) {
            addFile(file)
        }
        viewState.onDeleteUploadFile(id)
    }

    override fun onUploadAndOpen(path: String, title: String?, file: CloudFile, id: String) {
        viewState.onFileWebView(file)
    }

    override fun onUploadFileProgress(progress: Int, id: String, folderId: String) {
        if (id != null && folderId != null && mModelExplorerStack.currentId == folderId) {
            viewState.onUploadFileProgress(progress, id)
        }
    }

    override fun onUploadCanceled(path: String, info: String, id: String) {
        viewState.onSnackBar(info)
        viewState.onDeleteUploadFile(id)
        if (UploadWork.getUploadFiles(mModelExplorerStack.currentId)?.isEmpty() == true) {
            viewState.onRemoveUploadHead()
            getListWithHeaders(mModelExplorerStack.last(), true)
        }
    }

    override fun onUploadRepeat(path: String, info: String) {
        viewState.onDialogClose()
        viewState.onSnackBar(info)
    }

    fun onEditContextClick() {
        if (mItemClicked is CloudFile) {
            val file = mItemClicked as CloudFile
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
        if (mModelExplorerStack.countSelectedItems > 0) {
            val deleteShare = RequestDeleteShare()
            deleteShare.folderIds = mModelExplorerStack.selectedFoldersIds
            deleteShare.fileIds = mModelExplorerStack.selectedFilesIds
            mDisposable.add(Observable.fromCallable {
                api?.deleteShare(deleteShare)?.execute()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mModelExplorerStack.removeSelected()
                    resetDatesHeaders()
                    setPlaceholderType(if (mModelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                    viewState.onActionBarTitle("0")
                    viewState.onDeleteBatch(getListWithHeaders(mModelExplorerStack.last(), true))
                    onBatchOperations()
                }) { throwable: Throwable? -> fetchError(throwable) })
        }
    }

    fun removeShare() {
        if (mModelExplorerStack.countSelectedItems > 0) {
            viewState.onDialogQuestion(
                mContext.getString(R.string.dialogs_question_share_remove), null,
                TAG_DIALOG_ACTION_REMOVE_SHARE
            )
        } else {
            viewState.onSnackBar(mContext.getString(R.string.operation_empty_lists_data))
        }
    }

    val externalLink: Unit
        get() {
            if (mItemClicked != null) {
                mExternalAccessType = ApiContract.ShareType.READ
                val requestExternal = RequestExternal()
                requestExternal.share = mExternalAccessType
                mDisposable.add(mFileProvider.share(mItemClicked!!.id, requestExternal)
                    .subscribe({ responseExternal: ResponseExternal ->
                        mItemClicked!!.shared = !mItemClicked!!.shared
                        when (mExternalAccessType) {
                            ApiContract.ShareType.NONE -> viewState.onDocsAccess(
                                false,
                                mContext.getString(R.string.share_access_denied)
                            )
                            ApiContract.ShareType.READ, ApiContract.ShareType.READ_WRITE, ApiContract.ShareType.REVIEW -> {
                                KeyboardUtils.setDataToClipboard(
                                    mContext,
                                    responseExternal.response,
                                    mContext.getString(R.string.share_clipboard_external_link_label)
                                )
                                viewState.onDocsAccess(
                                    true,
                                    mContext.getString(R.string.share_clipboard_external_copied)
                                )
                            }
                        }
                    }) { throwable: Throwable? -> fetchError(throwable) })
            }
        }

    fun addToFavorite() {
        val requestFavorites = RequestFavorites()
        requestFavorites.fileIds = ArrayList(listOf(mItemClicked!!.id))
        mDisposable.add(mFileProvider.addToFavorites(requestFavorites)
            .subscribe({ response: Base? ->
                mItemClicked!!.favorite = !mItemClicked!!.favorite
                viewState.onSnackBar(mContext.getString(R.string.operation_add_to_favorites))
            }) { throwable: Throwable? -> fetchError(throwable) })
    }

    fun deleteFromFavorite() {
        val requestFavorites = RequestFavorites()
        requestFavorites.fileIds = ArrayList(listOf(mItemClicked!!.id))
        mDisposable.add(mFileProvider.deleteFromFavorites(requestFavorites)
            .subscribe({ response: Base? ->
                mItemClicked!!.favorite = !mItemClicked!!.favorite
                viewState.onRemoveItemFromFavorites()
                viewState.onSnackBar(mContext.getString(R.string.operation_remove_from_favorites))
            }) { throwable: Throwable? -> fetchError(throwable) })
    }

    fun removeFromFavorites() {
        if (mItemClicked != null) {
            mModelExplorerStack.removeItemById(mItemClicked!!.id)
        }
        viewState.onDocsGet(getListWithHeaders(mModelExplorerStack.last(), true))
    }

    fun removeShareContext() {
        if (mItemClicked != null) {
            val deleteShare = RequestDeleteShare()
            if (mItemClicked is CloudFolder) {
                deleteShare.folderIds = ArrayList(listOf((mItemClicked as CloudFolder).id))
            } else {
                deleteShare.fileIds = ArrayList(listOf(mItemClicked!!.id))
            }
            mDisposable.add(Observable.fromCallable {
                api?.deleteShare(deleteShare)?.execute()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (mItemClicked != null) {
                        mModelExplorerStack.removeItemById(mItemClicked!!.id)
                    }
                    setPlaceholderType(if (mModelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                    viewState.onDocsGet(getListWithHeaders(mModelExplorerStack.last(), true))
                    onBatchOperations()
                }) { throwable: Throwable? -> fetchError(throwable) })
        }
    }

    fun emptyTrash() {
        val explorer = mModelExplorerStack.last()
        if (explorer != null) {
            val provider = mFileProvider as CloudFileProvider
            showDialogProgress(true, TAG_DIALOG_CANCEL_BATCH_OPERATIONS)
            mBatchDisposable = provider.clearTrash()
                .switchMap { status }
                .subscribe(
                    { progress: Int? ->
                        viewState.onDialogProgress(
                            FileUtils.LOAD_MAX_PROGRESS,
                            progress!!
                        )
                    },
                    { throwable: Throwable? -> fetchError(throwable) }
                ) {
                    onBatchOperations()
                    refresh()
                }
        }
    }

    private fun checkMoveCopyFiles(action: String) {
        val filesIds = mOperationStack?.selectedFilesIds
        val foldersIds = mOperationStack?.selectedFoldersIds

        mDisposable.add((mFileProvider as CloudFileProvider).api.checkFiles(
            mDestFolderId ?: "",
            foldersIds,
            filesIds
        )
            .map { it.response }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when {
                    it.isNotEmpty() -> {
                        showMoveCopyDialog(it, action, mModelExplorerStack.currentTitle)
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
        if (mItemClicked is CloudFile) {
            val file = mItemClicked as CloudFile
            val extension = file.fileExst
            when (StringUtils.getExtension(extension)) {
                StringUtils.Extension.DOC, StringUtils.Extension.SHEET, StringUtils.Extension.PRESENTATION, StringUtils.Extension.PDF, StringUtils.Extension.FORM -> {
                    addRecent(mItemClicked as CloudFile)
                    //TODO open write mode
//                    file.isReadOnly = true
                    viewState.onFileWebView(file)
                }
                StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                    addRecent(mItemClicked as CloudFile)
                    viewState.onFileMedia(getListMedia(file.id), false)
                }
                else -> viewState.onFileDownloadPermission()
            }
            FirebaseUtils.addAnalyticsOpenEntity(networkSettings.getPortal(), extension)
        }
    }

    fun openFile(data: String) {
        val model = Json.decodeFromString<OpenDataModel>(data)
        mDisposable.add(mFileProvider.getFiles(model.folder?.id.toString(), getArgs(null))
            .map { loadSuccess(it) }
            .flatMap {
                mFileProvider.fileInfo(CloudFile().apply {
                    id = model.file?.id?.toString()
                })
            }
            .subscribe({ file: CloudFile ->
                mItemClicked = file
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
            ) { throwable: Throwable? ->
                fetchError(
                    throwable
                )
            })
    }

    private fun cancelRequest(id: String) {
        if (mGetDisposable.containsKey(id)) {
            val disposable = mGetDisposable.remove(id)
            disposable?.dispose()
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
        get() = StringUtils.equals(mModelExplorerStack.currentFolderOwnerId, account.id)

    private val isContextReadWrite: Boolean
        get() = isContextOwner || mModelExplorerStack.currentFolderAccess == ApiContract.ShareCode.READ_WRITE || mModelExplorerStack.currentFolderAccess == ApiContract.ShareCode.NONE

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
        get() = mItemClicked != null && mItemClicked!!.shared

    private val isClickedItemFavorite: Boolean
        get() = mItemClicked != null && mItemClicked!!.favorite

    private val isItemOwner: Boolean
        get() = mItemClicked != null && StringUtils.equals(mItemClicked!!.createdBy.id, account.id)

    private val isItemReadWrite: Boolean
        get() = mItemClicked != null && (mItemClicked!!.access == ApiContract.ShareCode.READ_WRITE || mItemClicked!!.access == ApiContract.ShareCode.NONE || mItemClicked!!.access == ApiContract.ShareCode.REVIEW)
    private val isItemEditable: Boolean
        get() = !isVisitor && !isProjectsSection && (isItemOwner || isItemReadWrite  ||
                mItemClicked?.access == ApiContract.ShareCode.REVIEW ||
                mItemClicked?.access == ApiContract.ShareCode.FILL_FORMS ||
                mItemClicked?.access == ApiContract.ShareCode.COMMENT)

    private val isItemShareable: Boolean
        get() = isItemEditable && (!isCommonSection || isAdmin) && !account.isPersonal() && !isProjectsSection
                && !isBunchSection && isItemReadWrite

    private val isClickedItemStorage: Boolean
        get() = mItemClicked != null && mItemClicked!!.providerItem

    //            getViewState().onActionBarTitle(mContext.getString(R.string.main_pager_docs_trash));
    var isTrashMode: Boolean
        get() = mIsTrashMode
        set(trashMode) {
            if (trashMode.also { mIsTrashMode = it }) {
//            getViewState().onActionBarTitle(mContext.getString(R.string.main_pager_docs_trash));
            }
        }

    private fun showDownloadFolderActivity(uri: Uri) {
        viewState.onDownloadActivity(uri)
    }

    fun setSectionType(sectionType: Int) {
        currentSectionType = sectionType
    }
}