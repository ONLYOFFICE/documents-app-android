package app.editors.manager.dropbox.mvp.presenters

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
import app.editors.manager.dropbox.dropbox.api.DropboxService
import app.editors.manager.dropbox.managers.providers.DropboxFileProvider
import app.editors.manager.dropbox.managers.utils.DropboxUtils
import app.editors.manager.dropbox.managers.works.DownloadWork
import app.editors.manager.dropbox.managers.works.UploadWork
import app.editors.manager.dropbox.mvp.views.DocsDropboxView
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.models.request.RequestCreate
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
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import java.util.*

class DocsDropboxPresenter: DocsBasePresenter<DocsDropboxView>(), UploadReceiver.OnUploadListener, DownloadReceiver.OnDownloadListener {

    private var downloadDisposable: Disposable? = null
    private var tempFile: CloudFile? = null

    private val workManager = WorkManager.getInstance(App.getApp())

    private val uploadReceiver: UploadReceiver
    private val downloadReceiver: DownloadReceiver


    val externalLink : Unit
        get() {
            itemClicked?.let {
                (fileProvider as DropboxFileProvider).share(it.id)?.let { externalLinkResponse ->
                    disposable.add(externalLinkResponse
                        .subscribe( {response ->
                            it.shared = !it.shared
                            response.link.let { link ->
                                KeyboardUtils.setDataToClipboard(
                                    context,
                                    link,
                                    context.getString(R.string.share_clipboard_external_link_label)
                                )
                            }
                            viewState.onDocsAccess(
                                true,
                                context.getString(R.string.share_clipboard_external_copied)
                            )
                        }) {throwable: Throwable -> fetchError(throwable)}
                    )
                }
            }
        }

    init {
        App.getApp().appComponent.inject(this)
        modelExplorerStack = ModelExplorerStack()
        filteringValue = ""
        placeholderViewType = PlaceholderViews.Type.NONE
        isContextClick = false
        isFilteringMode = false
        isSelectionMode = false
        isFoldersMode = false
        uploadReceiver = UploadReceiver()
        downloadReceiver = DownloadReceiver()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        uploadReceiver.setOnUploadListener(this)
        LocalBroadcastManager.getInstance(context).registerReceiver(uploadReceiver, uploadReceiver.filter)
        downloadReceiver.setOnDownloadListener(this)
        LocalBroadcastManager.getInstance(context).registerReceiver(downloadReceiver, downloadReceiver.filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(uploadReceiver)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadReceiver)
    }

    fun getProvider() {
        fileProvider?.let {
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
                    AccountUtils.getAccount(context, cloudAccount.getAccountName())?.let {
                        fileProvider = DropboxFileProvider()
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

    override fun createDownloadFile() {
        if (modelExplorerStack?.countSelectedItems!! == 0) {
            if (itemClicked is CloudFolder) {
                viewState.onCreateDownloadFile(DownloadWork.DOWNLOAD_ZIP_NAME)
            } else if (itemClicked is CloudFile) {
                viewState.onCreateDownloadFile((itemClicked as CloudFile).title)
            }
        } else {
            viewState.onChooseDownloadFolder()
        }
    }

    override fun download(downloadTo: Uri) {
        setBaseUrl(DropboxService.DROPBOX_BASE_URL_CONTENT)
        if (modelExplorerStack?.countSelectedItems!! == 0) {
            startDownload(downloadTo, itemClicked)
        } else {
            val itemList =  modelExplorerStack?.selectedFiles!! + modelExplorerStack?.selectedFolders!!
            itemList.forEach { item ->
                val fileName = if (item is CloudFile) item.title else DownloadWork.DOWNLOAD_ZIP_NAME
                val doc = DocumentFile.fromTreeUri(context, downloadTo)?.createFile(
                    StringUtils.getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf("."))), fileName
                )
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

    override fun filter(value: String, isSubmitted: Boolean): Boolean {
        setBaseUrl(DropboxService.DROPBOX_BASE_URL)
        return super.filter(value, isSubmitted)
    }

    override fun refresh(): Boolean {
        setBaseUrl(DropboxService.DROPBOX_BASE_URL)
        return super.refresh()
    }


    override fun getNextList() {
        val id = modelExplorerStack?.currentId
        val args = getArgs(filteringValue)
        fileProvider?.let { provider ->
            disposable.add(provider.getFiles(id!!, args).subscribe({ explorer: Explorer? ->
                modelExplorerStack?.addOnNext(explorer)
                val last = modelExplorerStack?.last()
                if (last != null) {
                    viewState.onDocsNext(getListWithHeaders(last, true))
                }
            }) { throwable: Throwable -> fetchError(throwable) })
        }
    }

    override fun createDocs(title: String) {
        setBaseUrl(DropboxService.DROPBOX_BASE_URL_CONTENT)
        val id = modelExplorerStack?.currentId
        id?.let {
            val requestCreate = RequestCreate()
            requestCreate.title = title
            fileProvider?.let { provider ->
                disposable.add(provider.createFile(id, requestCreate).subscribe({ file: CloudFile? ->
                    addFile(file)
                    setPlaceholderType(PlaceholderViews.Type.NONE)
                    viewState.onDialogClose()
                    viewState.onOpenLocalFile(file)
                }) { throwable: Throwable -> fetchError(throwable) })
            }
            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        }
    }


    fun upload(uri: Uri?, uris: ClipData?, tag: String) {
        setBaseUrl(DropboxService.DROPBOX_BASE_URL_CONTENT)
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
                .putString(UploadWork.TAG_FOLDER_ID, modelExplorerStack?.currentId)
                .putString(UploadWork.TAG_UPLOAD_FILES, uri.toString())
                .putString(UploadWork.KEY_TAG, tag)
                .build()

            val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
                .setInputData(data)
                .build()

            workManager.enqueue(request)
        }

    }

    override fun getArgs(filteringValue: String?): Map<String, String> {
        val args = mutableMapOf<String, String>()
        if(modelExplorerStack?.last()?.current?.providerItem == true) {
            args[DropboxUtils.DROPBOX_CONTINUE_CURSOR] =
                modelExplorerStack?.last()?.current?.parentId!!
        }
        if(modelExplorerStack?.last()?.current?.providerItem == true && this.filteringValue?.isNotEmpty() == true) {
            args[DropboxUtils.DROPBOX_SEARCH_CURSOR] =
                modelExplorerStack?.last()?.current?.parentId!!
        }
        args.putAll(super.getArgs(filteringValue))
        return args
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
        return if (super.copy()) {
            transfer(ApiContract.Operation.DUPLICATE, false)
            true
        } else {
            false
        }
    }

    override fun getFileInfo() {
        if (itemClicked != null && itemClicked is CloudFile) {
            val file = itemClicked as CloudFile
            val extension = file.fileExst
            if (StringUtils.isImage(extension)) {
                addRecent(file)
                return
            }
        }
        showDialogWaiting(TAG_DIALOG_CANCEL_UPLOAD)
        setBaseUrl(DropboxService.DROPBOX_BASE_URL_CONTENT)
        fileProvider?.let { provider ->
            downloadDisposable = provider.fileInfo(itemClicked)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { file: CloudFile? ->
                        tempFile = file
                        viewState.onDialogClose()
                        viewState.onOpenLocalFile(file)
                    }
                ) { throwable: Throwable -> fetchError(throwable) }
        }
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
                        ownerId = it.id,
                        source = it.portal
                    )
                )
            }
        }
    }

    override fun delete(): Boolean {
        setBaseUrl(DropboxService.DROPBOX_BASE_URL)
        if (modelExplorerStack?.countSelectedItems!! > 0) {
            viewState.onDialogQuestion(
                context.getString(R.string.dialogs_question_delete), null,
                TAG_DIALOG_BATCH_DELETE_SELECTED
            )
        } else {
            deleteItems()
        }
        return true
    }

    override fun updateViewsState() {
        if (isSelectionMode) {
            viewState.onStateUpdateSelection(true)
            viewState.onActionBarTitle(modelExplorerStack?.countSelectedItems.toString())
            viewState.onStateAdapterRoot(modelExplorerStack?.isNavigationRoot!!)
            viewState.onStateActionButton(false)
        } else if (isFilteringMode) {
            viewState.onActionBarTitle(context.getString(R.string.toolbar_menu_search_result))
            viewState.onStateUpdateFilter(true, filteringValue)
            viewState.onStateAdapterRoot(modelExplorerStack?.isNavigationRoot!!)
            viewState.onStateActionButton(false)
        } else if (!modelExplorerStack?.isRoot!!) {
            viewState.onStateAdapterRoot(false)
            viewState.onStateUpdateRoot(false)
            viewState.onStateActionButton(true)
            viewState.onActionBarTitle(if(currentTitle.isEmpty()) { itemClicked?.title } else { currentTitle } )
        } else {
            if (isFoldersMode) {
                viewState.onActionBarTitle(context.getString(R.string.operation_title))
                viewState.onStateActionButton(false)
            } else {
                viewState.onActionBarTitle("")
                viewState.onStateActionButton(true)
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
        state.isDocs = isClickedItemDocs
        state.isWebDav = false
        state.isOneDrive = false
        state.isGoogleDrive = false
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
        (fileProvider as DropboxFileProvider).refreshInstance()
    }

    override fun onDownloadError(id: String?, url: String?, title: String?, info: String?, uri: Uri?) {
        info?.let { viewState.onSnackBar(it) }
        viewState.onFinishDownload(uri)
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
    """.trimIndent(), context.getString(R.string.download_manager_open)
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
        if (modelExplorerStack?.currentId == folderId) {
            viewState.onUploadFileProgress(progress, id)
        }
    }

    override fun onUploadCanceled(path: String?, info: String?, id: String?) {
        info?.let { viewState.onSnackBar(it) }
        viewState.onDeleteUploadFile(id)
        if (app.editors.manager.managers.works.UploadWork.getUploadFiles(modelExplorerStack?.currentId)?.isEmpty() == true) {
            viewState.onRemoveUploadHead()
            getListWithHeaders(modelExplorerStack?.last(), true)
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