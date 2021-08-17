package app.editors.manager.onedrive.mvp.presenters

import android.net.Uri
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.account.Recent
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.onedrive.mvp.views.DocsOneDriveView
import app.editors.manager.onedrive.managers.providers.OneDriveFileProvider
import app.editors.manager.onedrive.managers.works.DownloadWork
import app.editors.manager.onedrive.managers.works.UploadWork
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
import moxy.InjectViewState
import java.util.*


@InjectViewState
class DocsOneDrivePresenter: DocsBasePresenter<DocsOneDriveView>() {

    private var downloadDisposable: Disposable? = null
    private var tempFile: CloudFile? = null
    private val workManager = WorkManager.getInstance()

    init {
        App.getApp().appComponent.inject(this)
        mModelExplorerStack = ModelExplorerStack()
        mFilteringValue = ""
        mPlaceholderType = PlaceholderViews.Type.NONE
        mIsContextClick = false
        mIsFilteringMode = false
        mIsSelectionMode = false
        mIsFoldersMode = false
    }

    fun getProvider() {
        mFileProvider?.let {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let {
                    withContext(Dispatchers.Main) {
                        getItemsById(null)
                    }

                }
            }
        } ?: run {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let { cloudAccount ->
                    AccountUtils.getAccount(mContext, cloudAccount.getAccountName())?.let {
                        mFileProvider = OneDriveFileProvider()
                        withContext(Dispatchers.Main) {
                            getItemsById(null)
                        }
                    }
                } ?: run {
                    throw Error("Not accounts")
                }
            }
        }
    }

    override fun download(downloadTo: Uri) {
        val data = Data.Builder()
            .putString(DownloadWork.FILE_ID_KEY, mItemClicked?.id)
            .putString(DownloadWork.FILE_URI_KEY, downloadTo.toString())
            .build()

        val request = OneTimeWorkRequest.Builder(DownloadWork::class.java)
            .setInputData(data)
            .build()

        workManager.enqueue(request)
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
        val id = mModelExplorerStack.currentId
        if (id != null) {
            val requestCreate = RequestCreate()
            requestCreate.title = title
            mDisposable.add(mFileProvider.createFile(id, requestCreate).subscribe({ file: CloudFile? ->
                addFile(file)
                setPlaceholderType(PlaceholderViews.Type.NONE)
                viewState.onDialogClose()
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
        showDialogWaiting(TAG_DIALOG_CANCEL_UPLOAD)
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

    fun upload(uri: Uri, tag: String) {
        val data = Data.Builder()
            .putString(UploadWork.KEY_FOLDER_ID, mModelExplorerStack.currentId)
            .putString(UploadWork.KEY_FROM, uri.toString())
            .putString(UploadWork.KEY_TAG, tag)
            .build()

        val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
            .setInputData(data)
            .build()

        workManager.enqueue(request)

    }

    override fun addRecent(file: CloudFile?) {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                file?.title?.let { it1 ->
                    Recent(
                        idFile = if (file.fileExst?.let { it1 -> StringUtils.isImage(it1) } == true) file.id else file.viewUrl,
                        path = file.webUrl,
                        name = it1,
                        size = file.pureContentLength,
                        isLocal = false,
                        isWebDav = true,
                        date = Date().time,
                        ownerId = it.id
                    )
                }?.let { it2 ->
                    recentDao.addRecent(
                        it2
                    )
                }
            }
        }
    }

    override fun onContextClick(item: Item?, position: Int, isTrash: Boolean) {
        onClickEvent(item, position)
        mIsContextClick = true
        val state = ContextBottomDialog.State()
        state.mTitle = itemClickedTitle
        state.mInfo = TimeUtils.formatDate(itemClickedDate)
        state.mIsFolder = !isClickedItemFile
        state.mIsDocs = isClickedItemDocs
        state.mIsWebDav = false
        state.mIsTrash = isTrash
        state.mIsItemEditable = true
        state.mIsContextEditable = true
        if (!isClickedItemFile) {
            state.mIconResId = R.drawable.ic_type_folder
        } else {
            state.mIconResId = getIconContext(
                StringUtils.getExtensionFromPath(
                    itemClickedTitle
                )
            )
        }
        state.mIsPdf = isPdf
        if (state.mIsShared && state.mIsFolder) {
            state.mIconResId = R.drawable.ic_type_folder_shared
        }
        viewState.onItemContext(state)
    }

    override fun onActionClick() {
        viewState.onActionDialog(false, true)
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
            viewState.onActionBarTitle(currentTitle)
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
    override fun move(): Boolean {
        return if (super.move()) {
            transfer(ApiContract.Operation.DUPLICATE, true)
            true
        } else {
            false
        }
    }
    override fun copy(): Boolean {
        return if (super.move()) {
            transfer(ApiContract.Operation.DUPLICATE, false)
            true
        } else {
            false
        }
    }
}