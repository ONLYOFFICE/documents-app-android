package app.editors.manager.googledrive.mvp.presenters

import android.content.ClipData
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.account.Recent
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.googledrive.managers.works.DownloadWork
import app.editors.manager.googledrive.managers.providers.GoogleDriveFileProvider
import app.editors.manager.googledrive.managers.utils.GoogleDriveUtils
import app.editors.manager.googledrive.managers.works.UploadWork
import app.editors.manager.googledrive.mvp.models.request.ShareRequest
import app.editors.manager.googledrive.mvp.views.DocsGoogleDriveView
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.onedrive.managers.providers.OneDriveFileProvider
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.mvp.models.request.ExternalLinkRequest
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import java.util.*

class DocsGoogleDrivePresenter: DocsBasePresenter<DocsGoogleDriveView>(), UploadReceiver.OnUploadListener, DownloadReceiver.OnDownloadListener {


    private var downloadDisposable: Disposable? = null
    private var tempFile: CloudFile? = null
    private val workManager = WorkManager.getInstance()
    private val uploadReceiver: UploadReceiver
    private val downloadReceiver: DownloadReceiver

    init {
        App.getApp().appComponent.inject(this)
        mModelExplorerStack = ModelExplorerStack()
        mFilteringValue = ""
        mPlaceholderType = PlaceholderViews.Type.NONE
        mIsContextClick = false
        mIsFilteringMode = false
        mIsSelectionMode = false
        mIsFoldersMode = false
        uploadReceiver = UploadReceiver()
        downloadReceiver = DownloadReceiver()
    }


    val externalLink : Unit
        get() {
            val request = ShareRequest(
                role = "reader",
                type = "anyone"
            )
            val externalLink = if (mItemClicked is CloudFile) (mItemClicked as CloudFile).webUrl else if(mItemClicked is GoogleDriveFolder) (mItemClicked as GoogleDriveFolder).webUrl else ""
            mDisposable.add(
                mItemClicked?.id?.let { id ->
                    (mFileProvider as GoogleDriveFileProvider).share(id, request)
                        .subscribe({ response ->
                            if(response) {
                                KeyboardUtils.setDataToClipboard(
                                    mContext,
                                    externalLink,
                                    mContext.getString(R.string.share_clipboard_external_link_label)
                                )
                                viewState.onDocsAccess(
                                    true,
                                    mContext.getString(R.string.share_clipboard_external_copied)
                                )
                            } else {
                                viewState.onDocsAccess(
                                    false,
                                    mContext.getString(R.string.errors_client_forbidden)
                                )
                            }
                        },
                            {throwable: Throwable -> fetchError(throwable)}
                        )
                }!!
            )


        }

    fun getProvider() {
        mFileProvider?.let {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let {
                    withContext(Dispatchers.Main) {
                        getItemsById("root")
                    }

                }
            }
        } ?: run {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let { cloudAccount ->
                    AccountUtils.getAccount(mContext, cloudAccount.getAccountName())?.let {
                        mFileProvider = GoogleDriveFileProvider()
                        withContext(Dispatchers.Main) {
                            getItemsById("root")
                        }
                    }
                } ?: run {
                    throw Error("Not accounts")
                }
            }
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        uploadReceiver.setOnUploadListener(this)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(uploadReceiver, uploadReceiver.filter)
        downloadReceiver.setOnDownloadListener(this)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(downloadReceiver, downloadReceiver.filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(uploadReceiver)
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(downloadReceiver)
    }

    override fun getNextList() {
        val id = mModelExplorerStack.currentId
        val args = getArgs(mFilteringValue)
        mDisposable.add(mFileProvider.getFiles(id!!, args).subscribe({ explorer: Explorer? ->
            mModelExplorerStack.addOnNext(explorer)
            val last = mModelExplorerStack.last()
            if (last != null) {
                viewState.onDocsNext(getListWithHeaders(last, true))
            }
        }) { throwable: Throwable? -> fetchError(throwable) })
    }

    override fun getArgs(filteringValue: String?): MutableMap<String, String> {
        val args = mutableMapOf<String, String>()
        if(mModelExplorerStack?.last()?.current?.providerItem == true) {
            args[GoogleDriveUtils.GOOGLE_DRIVE_NEXT_PAGE_TOKEN] =
                mModelExplorerStack?.last()?.current?.parentId!!
        }
        args.putAll(super.getArgs(filteringValue))
        return args
    }

    override fun createDocs(title: String) {
        val id = mModelExplorerStack.currentId
        id?.let {
            val requestCreate = RequestCreate()
            requestCreate.title = title
            mDisposable.add(mFileProvider.createFile(id, requestCreate).subscribe({ file: CloudFile? ->
                addFile(file)
                setPlaceholderType(PlaceholderViews.Type.NONE)
                viewState.onDialogClose()
                //viewState.onOpenLocalFile(file)
            }) { throwable: Throwable? -> fetchError(throwable) })
            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        }
    }

    override fun getFileInfo() {
        if (mItemClicked != null && mItemClicked is CloudFile) {
            val file = mItemClicked as CloudFile
            val extension = file.fileExst
            if (StringUtils.isImage(extension)) {
                addRecent(file)
                return
            }
        }
        downloadDisposable = mFileProvider.fileInfo(mItemClicked!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { file: CloudFile? ->
                    tempFile = file
                    viewState.onDialogClose()
                    viewState.onOpenLocalFile(file)
                }
            ) { throwable: Throwable? -> fetchError(throwable) }
    }

    override fun move(): Boolean {
        return if (super.move()) {
            transfer(ApiContract.Operation.DUPLICATE, true)
            true
        } else {
            false
        }
    }

    override fun copy(): Boolean {
        val itemList = mutableListOf<Item>()
        when {
            mModelExplorerStack?.selectedFiles?.isNotEmpty() == true -> {
                itemList.addAll(mModelExplorerStack?.selectedFiles!!)
            }
            mModelExplorerStack.selectedFolders.isNotEmpty() -> {
                viewState.onSnackBar(mContext.getString(R.string.storage_google_drive_copy_folder_error))
                itemList.addAll(mModelExplorerStack?.selectedFiles!!)
            }
            else -> {
                mItemClicked?.let { itemList.add(it) }
            }
        }
        showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        mDisposable.add((mFileProvider as GoogleDriveFileProvider).copy(itemList, mModelExplorerStack.currentId!!)
            .subscribe ({},{
                fetchError(it)
                if (mIsSelectionMode) {
                    setSelection(false)
                    updateViewsState()
                }
                false
            }, {
                viewState.onDocsBatchOperation()
                if (mIsSelectionMode) {
                    setSelection(false)
                    updateViewsState()
                }
                true
            })
        )
        return false
    }

    override fun copySelected() {
        copy()
    }

    override fun addRecent(file: CloudFile) {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                recentDao.addRecent(
                    Recent(
                        idFile = if (StringUtils.isImage(file.fileExst)) file.id else file.viewUrl,
                        path = file.webUrl,
                        name = file.title,
                        size = file.pureContentLength,
                        isLocal = false,
                        isWebDav = true,
                        date = Date().time,
                        ownerId = it.id
                    )
                )
            }
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
            viewState.onStateActionButton(true)
            viewState.onActionBarTitle(if(currentTitle.isEmpty()) { mItemClicked?.title } else { currentTitle } )
        } else {
            if (mIsFoldersMode) {
                viewState.onActionBarTitle(mContext.getString(R.string.operation_title))
                viewState.onStateActionButton(false)
            } else {
                viewState.onActionBarTitle("")
                viewState.onStateActionButton(true)
            }
            viewState.onStateAdapterRoot(true)
            viewState.onStateUpdateRoot(true)
        }
    }


    override fun createDownloadFile() {
        if(mModelExplorerStack.countSelectedItems == 0) {
            if (mItemClicked is CloudFolder) {
                viewState.onCreateDownloadFile(DownloadWork.DOWNLOAD_ZIP_NAME)
            } else if (mItemClicked is CloudFile) {
                viewState.onCreateDownloadFile((mItemClicked as CloudFile).title)
            }
        } else {
            viewState.onChooseDownloadFolder()
        }
    }

    override fun download(downloadTo: Uri) {
        if(mModelExplorerStack.countSelectedItems == 0) {
            startDownload(downloadTo, mItemClicked)
        } else {
            val itemList: MutableList<Item> = (mModelExplorerStack.selectedFiles + mModelExplorerStack.selectedFolders).toMutableList()
            itemList.forEach { item ->
                val fileName = if(item is CloudFile) item.title else DownloadWork.DOWNLOAD_ZIP_NAME
                val doc = DocumentFile.fromTreeUri(mContext, downloadTo)?.createFile(StringUtils.getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf("."))), fileName)
                startDownload(doc?.uri!!, item)
            }
        }
    }

    private fun startDownload(downloadTo: Uri, item: Item?) {
        val data = Data.Builder()
            .putString(DownloadWork.FILE_ID_KEY, item?.id)
            .putString(DownloadWork.FILE_URI_KEY, downloadTo.toString())
            .putString(
                DownloadWork.DOWNLOADABLE_ITEM_KEY,
                if (item is CloudFile) DownloadWork.DOWNLOADABLE_ITEM_FILE else DownloadWork.DOWNLOADABLE_ITEM_FOLDER
            )
            .build()

        val request = OneTimeWorkRequest.Builder(DownloadWork::class.java)
            .setInputData(data)
            .build()

        workManager.enqueue(request)
    }

    override fun delete(): Boolean {
        if (mModelExplorerStack.countSelectedItems > 0) {
            viewState.onDialogQuestion(
                mContext.getString(R.string.dialogs_question_delete), null,
                TAG_DIALOG_BATCH_DELETE_SELECTED
            )
        } else {
            deleteItems()
        }
        return true
    }

    fun upload(uri: Uri?, uris: ClipData?, tag: String) {
        val uploadUris = mutableListOf<Uri>()
        var index = 0

        if(uri != null) {
            uploadUris.add(uri)
        } else if(uris != null) {
            while(index != uris.itemCount) {
                uploadUris.add(uris.getItemAt(index).uri)
                index++
            }
        }

        for (uri in uploadUris) {
            val data = Data.Builder()
                .putString(UploadWork.KEY_FOLDER_ID, mModelExplorerStack.currentId)
                .putString(UploadWork.KEY_FROM, uri.toString())
                .putString(UploadWork.KEY_TAG, tag)
                .putString(UploadWork.KEY_FILE_ID, mItemClicked?.id)
                .build()

            val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
                .setInputData(data)
                .build()

            workManager.enqueue(request)
        }

    }

    override fun onContextClick(item: Item?, position: Int, isTrash: Boolean) {
        onClickEvent(item, position)
        mIsContextClick = true
        val state = ContextBottomDialog.State()
        state.title = itemClickedTitle
        state.info = TimeUtils.formatDate(itemClickedDate)
        state.isFolder = !isClickedItemFile
        state.isDocs = isClickedItemDocs
        state.isWebDav = false
        state.isOneDrive = false
        state.isDropBox = false
        state.isGoogleDrive = true
        state.isTrash = isTrash
        state.isItemEditable = true
        state.isContextEditable = true
        state.isCanShare = true
        if (!isClickedItemFile) {
            state.iconResId = R.drawable.ic_type_folder
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
        viewState.onActionDialog(false, true)
    }

    override fun onDownloadError(id: String?, url: String?, title: String?, info: String?) {
        info?.let { viewState.onSnackBar(it) }
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
        uri: Uri?
    ) {
        viewState.onDialogClose()
        viewState.onSnackBarWithAction(
            """
    $info
    $title
    """.trimIndent(), mContext.getString(R.string.download_manager_open)
        ) { showDownloadFolderActivity(uri) }
    }

    override fun onDownloadCanceled(id: String?, info: String?) {
        viewState.onDialogClose()
        info?.let { viewState.onSnackBar(it) }
    }

    override fun onDownloadRepeat(id: String?, title: String?, info: String?) {
        viewState.onDialogClose()
        info?.let { viewState.onSnackBar(it) }
    }

    override fun onUploadError(path: String?, info: String?, file: String?) {
        info?.let { viewState.onSnackBar(it) }
    }

    override fun onUploadComplete(
        path: String?,
        info: String?,
        title: String?,
        file: CloudFile?,
        id: String?
    ) {
        info?.let { viewState.onSnackBar(it) }
        refresh()
        viewState.onDeleteUploadFile(id)
    }

    override fun onUploadAndOpen(path: String?, title: String?, file: CloudFile?, id: String?) {
        TODO("Not yet implemented")
    }

    override fun onUploadFileProgress(progress: Int, id: String?, folderId: String?) {
        if (mModelExplorerStack.currentId == folderId) {
            viewState.onUploadFileProgress(progress, id)
        }
    }

    override fun onUploadCanceled(path: String?, info: String?, id: String?) {
        info?.let { viewState.onSnackBar(it) }
        viewState.onDeleteUploadFile(id)
        if (app.editors.manager.managers.works.UploadWork.getUploadFiles(mModelExplorerStack.currentId)?.isEmpty() == true) {
            viewState.onRemoveUploadHead()
            getListWithHeaders(mModelExplorerStack.last(), true)
        }
    }

    override fun onUploadRepeat(path: String?, info: String?) {
        viewState.onDialogClose()
        info?.let { viewState.onSnackBar(it) }
    }
    private fun showDownloadFolderActivity(uri: Uri?) {
        viewState.onDownloadActivity(uri)
    }
}