package app.editors.manager.dropbox.mvp.presenters

import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.account.Recent
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.dropbox.dropbox.api.DropboxService
import app.editors.manager.dropbox.managers.providers.DropboxFileProvider
import app.editors.manager.dropbox.managers.utils.DropboxUtils
import app.editors.manager.dropbox.mvp.views.DocsDropboxView
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
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
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import java.util.*
import app.editors.manager.dropbox.managers.works.*
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.onedrive.managers.providers.OneDriveFileProvider
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.mvp.models.request.ExternalLinkRequest
import lib.toolkit.base.managers.utils.KeyboardUtils

class DocsDropboxPresenter: DocsBasePresenter<DocsDropboxView>(), UploadReceiver.OnUploadListener, DownloadReceiver.OnDownloadListener {

    private var downloadDisposable: Disposable? = null
    private var tempFile: CloudFile? = null

    private val workManager = WorkManager.getInstance()

    private val uploadReceiver: UploadReceiver
    private val downloadReceiver: DownloadReceiver


    val externalLink : Unit
        get() {
            mItemClicked?.let {
                (mFileProvider as DropboxFileProvider).share(it.id)?.let { externalLinkResponse ->
                    mDisposable.add(externalLinkResponse
                        .subscribe( {response ->
                            it.shared = !it.shared
                            response.link.let { link ->
                                KeyboardUtils.setDataToClipboard(
                                    mContext,
                                    link,
                                    mContext.getString(R.string.share_clipboard_external_link_label)
                                )
                            }
                            viewState.onDocsAccess(
                                true,
                                mContext.getString(R.string.share_clipboard_external_copied)
                            )
                        }) {throwable: Throwable -> fetchError(throwable)}
                    )
                }
            }
        }

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

    fun getProvider() {
        mFileProvider?.let {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let {
                    withContext(Dispatchers.Main) {
                        setBaseUrl(DropboxService.DROPBOX_BASE_URL)
                        getItemsById(DropboxUtils.DROPBOX_ROOT)
                    }

                }
            }
        } ?: run {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let { cloudAccount ->
                    AccountUtils.getAccount(mContext, cloudAccount.getAccountName())?.let {
                        mFileProvider = DropboxFileProvider()
                        withContext(Dispatchers.Main) {
                            setBaseUrl(DropboxService.DROPBOX_BASE_URL)
                            getItemsById(DropboxUtils.DROPBOX_ROOT)
                        }
                    }
                } ?: run {
                    throw Error("Not accounts")
                }
            }
        }
    }

    override fun download(downloadTo: Uri) {
        setBaseUrl(DropboxService.DROPBOX_BASE_URL_CONTENT)
        val data = Data.Builder()
            .putString(DownloadWork.FILE_ID_KEY, mItemClicked?.id)
            .putString(DownloadWork.FILE_URI_KEY, downloadTo.toString())
            .build()

        val request = OneTimeWorkRequest.Builder(DownloadWork::class.java)
            .setInputData(data)
            .build()

        workManager.enqueue(request)
    }

    override fun refresh(): Boolean {
        setBaseUrl(DropboxService.DROPBOX_BASE_URL)
        return super.refresh()
    }


    override fun getNextList() {
        TODO("Not yet implemented")
    }

    override fun createDocs(title: String) {
        TODO("Not yet implemented")
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
        showDialogWaiting(TAG_DIALOG_CANCEL_UPLOAD)
        setBaseUrl(DropboxService.DROPBOX_BASE_URL_CONTENT)
        downloadDisposable = mFileProvider.fileInfo(mItemClicked)
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
        state.isDropBox = true
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

    private fun setBaseUrl(baseUrl: String) {
        networkSettings.setBaseUrl(baseUrl)
        (mFileProvider as DropboxFileProvider).refreshInstance()
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
        TODO("Not yet implemented")
    }

    override fun onUploadComplete(
        path: String?,
        info: String?,
        title: String?,
        file: CloudFile?,
        id: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun onUploadAndOpen(path: String?, title: String?, file: CloudFile?, id: String?) {
        TODO("Not yet implemented")
    }

    override fun onUploadFileProgress(progress: Int, id: String?, folderId: String?) {
        TODO("Not yet implemented")
    }

    override fun onUploadCanceled(path: String?, info: String?, id: String?) {
        TODO("Not yet implemented")
    }

    override fun onUploadRepeat(path: String?, info: String?) {
        TODO("Not yet implemented")
    }

    private fun showDownloadFolderActivity(uri: Uri?) {
        viewState.onDownloadActivity(uri)
    }
}