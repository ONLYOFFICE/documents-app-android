package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.content.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.account.AccountDao
import app.documents.core.account.RecentDao
import app.documents.core.network.ApiContract
import app.documents.core.settings.NetworkSettings
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.exceptions.NoConnectivityException
import app.editors.manager.managers.providers.BaseFileProvider
import app.editors.manager.managers.providers.CloudFileProvider
import app.editors.manager.managers.providers.ProviderError
import app.editors.manager.managers.providers.ProviderError.Companion.throwInterruptException
import app.editors.manager.managers.providers.WebDavFileProvider
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.FirebaseUtils.addAnalyticsCreateEntity
import app.editors.manager.managers.utils.FirebaseUtils.addCrash
import app.editors.manager.managers.works.DownloadWork
import app.editors.manager.managers.works.UploadWork
import app.editors.manager.managers.works.UploadWork.Companion.getUploadFiles
import app.editors.manager.managers.works.UploadWork.Companion.putNewUploadFiles
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.models.models.ExplorerStackMap
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.models.request.RequestDownload
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.ui.views.custom.PlaceholderViews
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.ContentResolverUtils.getName
import lib.toolkit.base.managers.utils.ContentResolverUtils.getSize
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.NetworkUtils.isOnline
import lib.toolkit.base.managers.utils.NetworkUtils.isWifiEnable
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.equals
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getFormattedSize
import lib.toolkit.base.managers.utils.StringUtils.getJsonObject
import lib.toolkit.base.managers.utils.StringUtils.getNameWithoutExtension
import lib.toolkit.base.managers.utils.StringUtils.isDocument
import lib.toolkit.base.managers.utils.StringUtils.isImage
import lib.toolkit.base.managers.utils.StringUtils.isVideoSupport
import lib.toolkit.base.managers.utils.TimeUtils.monthMs
import lib.toolkit.base.managers.utils.TimeUtils.todayMs
import lib.toolkit.base.managers.utils.TimeUtils.weekMs
import lib.toolkit.base.managers.utils.TimeUtils.yearMs
import lib.toolkit.base.managers.utils.TimeUtils.yesterdayMs
import moxy.InjectViewState
import moxy.MvpPresenter
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.HttpException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException


@InjectViewState
abstract class DocsBasePresenter<View : DocsBaseView> : MvpPresenter<View>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var preferenceTool: PreferenceTool

    @Inject
    lateinit var operationsState: OperationsState

    @Inject
    lateinit var accountDao: AccountDao

    @Inject
    lateinit var networkSettings: NetworkSettings

    @Inject
    lateinit var recentDao: RecentDao

    /**
     * Handler for some common job
     * */

    private val handler = Handler()

    /**
     * Saved values
     * */

    protected var fileProvider: BaseFileProvider? = null
    protected var modelExplorerStack: ModelExplorerStack? = null
    protected var filteringValue: String? = null
    protected var placeholderViewType: PlaceholderViews.Type? = null
    protected var destFolderId: String? = null
    protected var operationStack: ExplorerStackMap? = null
    private var isSubmitted = false
    private var uploadUri: Uri? = null

    /**
     * Modes
     * */

    var isFoldersMode = false
    var isTrashMode = false
    var isFilteringMode = false
        protected set
    var isSelectionMode = false
        protected set

    /**
     * Open in edit mode
     * */

    /**
     * Clicked/Checked and etc...
     * */

    private var itemClickedPosition = 0
    protected var isContextClick = false
    var itemClicked: Item? = null
        protected set

    /**
     * Headers date
     * */

    private var isFolderHeader = false
    private var isFileHeader = false
    private var isCreatedHeader = false
    private var isTodayHeader = false
    private var isYesterdayHeader = false
    private var isWeekHeader = false
    private var isMonthHeader = false
    private var isYearHeader = false
    private var isMoreYearHeader = false

    /**
     * Get docs
     * */

    private val getDisposable: Disposable? = null

    /**
     * Tasks for async job
     * */

    protected var disposable = CompositeDisposable()
    protected var batchDisposable: Disposable? = null
    protected var uploadDisposable: Disposable? = null
    private var filterRun: Runnable? = null
    private var isTerminate = false
    protected var downloadDisposable: Disposable? = null
    private var isAccessDenied = false

    /**
     * Download WorkManager
     */

    private var downloadManager = WorkManager.getInstance()
    private var isMultipleDelete = false

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        disposable.dispose()
        fileProvider = null
    }

    fun getItemsById(id: String?) {
        id?.let {
            setPlaceholderType(PlaceholderViews.Type.LOAD)
            fileProvider?.let { provider ->
                disposable.add(
                    provider.getFiles(id, getArgs(null))
                        .subscribe( { explorer: Explorer? -> loadSuccess(explorer) }, this::fetchError))
            }
        }
    }

    open fun refresh(): Boolean {
        setPlaceholderType(PlaceholderViews.Type.LOAD)
        modelExplorerStack?.let { stack ->
            stack.currentId?.let { id ->
                fileProvider?.let { provider ->
                    disposable.add(
                        provider.getFiles(id, getArgs(filteringValue))
                            .subscribe({ explorer ->
                                stack.refreshStack(explorer)
                                updateViewsState()
                                viewState.onDocsRefresh(getListWithHeaders(stack.last(), true))
                            }, this::fetchError)
                    )
                    viewState.onSwipeEnable(true)
                    return true
                }
            }
        }
        return false
    }

    open fun sortBy(value: String, isRepeatedTap: Boolean): Boolean {
        preferenceTool.sortBy = value
        if (isRepeatedTap) {
            reverseSortOrder()
        }
        return refresh()
    }

    protected fun reverseSortOrder() {
        if (preferenceTool.sortOrder == ApiContract.Parameters.VAL_SORT_ORDER_ASC) {
            preferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_DESC
            viewState.onReverseSortOrder(ApiContract.Parameters.VAL_SORT_ORDER_DESC)
        } else {
            preferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_ASC
            viewState.onReverseSortOrder(ApiContract.Parameters.VAL_SORT_ORDER_ASC)
        }
    }

    open fun orderBy(value: String): Boolean {
        preferenceTool.sortOrder = value
        return refresh()
    }

    open fun filter(value: String, isSubmitted: Boolean): Boolean {
        if (isFilteringMode) {
            this.isSubmitted = isSubmitted
            modelExplorerStack?.let { stack ->
                 stack.currentId?.let { id ->
                     filteringValue = value
                     fileProvider?.let { provider ->
                         provider.getFiles(id, getArgs(value))
                             .debounce(FILTERING_DELAY.toLong(), TimeUnit.MILLISECONDS)
                             .subscribe({ explorer ->
                                 modelExplorerStack?.setFilter(explorer)
                                 setPlaceholderType(if (modelExplorerStack?.isListEmpty == true) PlaceholderViews.Type.SEARCH else
                                     PlaceholderViews.Type.NONE)
                                 updateViewsState()
                                 viewState.onDocsFilter(getListWithHeaders(modelExplorerStack?.last(), true))
                             }, this::fetchError)
                     }
                 }
            }
        }
        return false
    }

    fun filterWait(value: String) {
        if (!isSubmitted) {
            filterRun?.let { runnable ->
                handler.removeCallbacks(runnable)
            }

            filterRun = Runnable { filter(value, false) }.apply {
                handler.postDelayed(this, FILTERING_DELAY.toLong())
            }
        }
    }

    /**
     * Change docs
     * */

    fun createFolder(title: String?) {
        preferenceTool.portal?.let {
            addAnalyticsCreateEntity(it, false, null)
        }

        modelExplorerStack?.currentId?.let { id ->
            val requestCreate = RequestCreate().apply {
                setTitle(title?.takeIf { title.isNotEmpty() } ?: context.getString(R.string.dialogs_edit_create_docs))
            }

            fileProvider?.let { provider ->
                provider.createFolder(id, requestCreate)
                    .subscribe({ folder ->
                        setPlaceholderType(PlaceholderViews.Type.NONE)
                        viewState.onDialogClose()
                        addFolder(folder)
                    }, this::fetchError)
            }

            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        }
    }

    private fun renameFolder(id: Item, title: String) {
        modelExplorerStack?.currentId?.let { currentId ->
            fileProvider?.let { provider ->
                disposable.add(
                    provider.rename(id, title, null)
                        .flatMap { provider.getFiles(currentId, getArgs(null)) }
                        .subscribe({ item ->
                            viewState.onDialogClose()
                            viewState.onSnackBar(context.getString(R.string.list_context_rename_success))
                            loadSuccess(item)
                        }, this::fetchError))
            }
        }
    }

    private fun renameFile(id: Item, title: String, version: Int) {
        modelExplorerStack?.currentId?.let { currentId ->
            fileProvider?.let { provider ->
                disposable.add(
                    provider.rename(id, title, version)
                        .flatMap { provider.getFiles(currentId, getArgs(null)) }
                        .subscribe({ item ->
                            viewState.onDialogClose()
                            viewState.onSnackBar(context.getString(R.string.list_context_rename_success))
                            loadSuccess(item)
                        }, this::fetchError))
            }
        }
    }


    protected fun loadSuccess(explorer: Explorer?) {
        modelExplorerStack?.let { stack ->
            stack.addStack(explorer)
            updateViewsState()
            setPlaceholderType(if (stack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
            stack.last()?.let { last -> viewState.onDocsGet(getListWithHeaders(last, true)) }
        }
    }

    open fun deleteItems() {
        modelExplorerStack?.let { stack ->
            val items = mutableListOf<Item>().apply {
                if (stack.countSelectedItems > 0) {
                    addAll(stack.selectedFiles)
                    addAll(stack.selectedFolders)
                } else if (itemClicked != null) {
                    stack.getItemById(itemClicked)?.let { item -> add(item) }
                }
            }

            showDialogProgress(true, TAG_DIALOG_CANCEL_BATCH_OPERATIONS)
            fileProvider?.let { provider ->
                batchDisposable = provider.delete(items, null)
                    .switchMap { status }
                    .subscribe({ progress ->
                        viewState.onDialogProgress(100, progress ?: 0)
                    }, this::fetchError) {

                        if (stack.countSelectedItems > 0) {
                            stack.removeSelected()
                            getBackStack()
                        } else if (itemClicked != null) {
                            stack.removeItemById(itemClicked?.id)
                        }

                        resetDatesHeaders()
                        setPlaceholderType(if (stack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                        viewState.onDeleteBatch(getListWithHeaders(stack.last(), true))

                        if (isMultipleDelete) {
                            onFileDeleteProtected()
                            isMultipleDelete = false
                        } else {
                            onBatchOperations()
                        }
                    }
            }
        }
    }

    open fun delete(): Boolean {
        modelExplorerStack?.let { stack ->
            if (stack.countSelectedItems > 0) {
                for (item in stack.selectedFiles) {
                    isFileDeleteProtected(item)?.let { observable ->
                        disposable.add(
                            observable.subscribe({ isFileProtected ->
                                if (isFileProtected) {
                                    isMultipleDelete = true
                                    stack.setSelectById(item, false)
                                }
                            }, this::fetchError)
                        )
                    }
                }
                viewState.onDialogQuestion(
                    context.getString(R.string.dialogs_question_delete), null,
                    TAG_DIALOG_BATCH_DELETE_SELECTED
                )
            } else if (!isSelectionMode) {
                if (itemClicked is CloudFile) {
                    fileProvider?.let { provider ->
                        disposable.add(
                            getFileInfo(provider, itemClicked as CloudFile).subscribe({ response ->
                                if (response.fileStatus.isNotEmpty()) {
                                    val statusMask = response.fileStatus.toInt() and ApiContract.FileStatus.IS_EDITING
                                    if (statusMask != 0) {
                                        onFileDeleteProtected()
                                    } else {
                                        deleteItems()
                                    }
                                } else {
                                    deleteItems()
                                }
                            }, this::fetchError)
                        )
                    }
                } else {
                    deleteItems()
                }
            } else {
                viewState.onSnackBar(context.getString(R.string.operation_empty_lists_data))
            }
        }
        return true
    }

    private fun getFileInfo(provider: BaseFileProvider, itemClicked: CloudFile): Observable<CloudFile> {
        return if (provider is WebDavFileProvider) provider.fileInfo(itemClicked, false) else
            provider.fileInfo(itemClicked)
    }

    private fun isFileDeleteProtected(item: Item): Observable<Boolean>? {
        return fileProvider?.let { provider ->
            Observable.just(provider.fileInfo(item))
                .flatMap { response: Observable<CloudFile> ->
                    response.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap { file: CloudFile ->
                            val fileStatus = if (file.fileStatus.isEmpty())
                                ApiContract.FileStatus.NONE else file.fileStatus.toInt()
                            val statusMask = fileStatus and ApiContract.FileStatus.IS_EDITING
                            if (statusMask != 0) {
                                Observable.just(true)
                            } else {
                                Observable.just(false)
                            }
                        }
                }
        }
    }

    open fun move(): Boolean {
        modelExplorerStack?.currentId?.also { destFolder ->
            destFolderId = destFolder
            operationStack?.let { stack ->
                if (destFolder == stack.currentId) {
                    viewState.onError(context.getString(R.string.operation_error_move_to_same))
                    return false
                }
                if (stack.selectedItems > 0) {
                    if (isContainsPathInPath(stack.selectedFoldersIds)) {
                        viewState.onError(context.getString(R.string.operation_error_move_to_same_subfolder))
                        return false
                    }
                    return true
                }
            }
        }
        return false
    }

    fun transfer(conflict: Int, isMove: Boolean) {
        operationStack?.let { operationStack ->
            val destination = CloudFolder().apply { id = destFolderId }

            val items = mutableListOf<Item>().apply {
                addAll(operationStack.selectedFiles)
                addAll(operationStack.selectedFolders)
            }

            fileProvider?.let { provider ->
                batchDisposable = provider.transfer(items, destination, conflict, isMove, false)?.let { observable ->
                    observable.switchMap { status }
                        .subscribe(
                            { progress: Int? -> viewState.onDialogProgress(100, progress ?: 0) },
                            this::fetchError
                        ) {
                            modelExplorerStack?.let { stack ->
                                if (!operationStack.currentId.equals(destFolderId, ignoreCase = true)) {
                                    operationStack.setSelectionAll(false)
                                    operationStack.explorer?.also {
                                        it.destFolderId = destFolderId
                                        operationsState.insert(stack.rootFolderType, it.takeIf {
                                            stack.rootFolderType== ApiContract.SectionType.CLOUD_USER
                                        } ?: setAccess(it))
                                    }
                                }
                                setPlaceholderType(if (stack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                                onBatchOperations()
                            }
                        }
                }
            }

            showDialogWaiting(TAG_DIALOG_CANCEL_BATCH_OPERATIONS)
        }
    }

    val status: Observable<Int>
        get() = Observable.create { emitter: ObservableEmitter<Int> ->
            do {
                try {
                    if (isTerminate && batchDisposable?.isDisposed == true) {
                        terminateOperation()
                        break
                    }
                    val response = fileProvider?.getStatusOperation()?.response
                    if (response?.isNotEmpty() == true) {
                        Log.d(TAG, "getStatus: " + response[0].id)
                        emitter.onNext(response[0].progress)
                    } else {
                        emitter.onComplete()
                        break
                    }
                } catch (e: Exception) {
                    emitter.onError(throwInterruptException())
                    break
                }
            } while (true)
        }.subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())

    private fun terminateOperation() {
        fileProvider?.terminate()?.let { provider ->
            disposable.add(
                provider
                    .doOnSubscribe { showDialogProgress(true, TAG_DIALOG_BATCH_TERMINATE) }
                    .subscribe({
                        isTerminate = false
                        onBatchOperations()
                    }, this::fetchError))
        }
    }

    open fun copy(): Boolean {
        modelExplorerStack?.currentId?.also { destFolder ->
            destFolderId = destFolder
            operationStack?.let { stack ->
                if (destFolder == stack.currentId) {
                    viewState.onError(context.getString(R.string.operation_error_move_to_same))
                    return false
                }

                if (stack.selectedItems > 0) {
                    if (isContainsPathInPath(stack.selectedFoldersIds)) {
                        viewState.onError(context.getString(R.string.operation_error_move_to_same_subfolder))
                        return false
                    }
                    return true
                }
            }
        }
        return false
    }

    open fun terminate() {
        batchDisposable?.let { disposable ->
            isTerminate = true
            disposable.dispose()
            viewState.onDialogClose()
            refresh()
        }
    }

    open fun rename(title: String?) {
        itemClicked?.let { item ->
            if (title?.isNotEmpty() == true) {
                modelExplorerStack?.getItemById(item)?.let { stackItem ->
                    when (stackItem) {
                        is CloudFolder -> renameFolder(stackItem, title)
                        is CloudFile -> renameFile(stackItem, title, stackItem.nextVersion)
                    }
                    showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
                }
            }
        }
    }

    /**
     * Downloads/Uploads
     * */

    open fun createDownloadFile() {
        modelExplorerStack?.let { stack ->
            if (stack.selectedFiles.isNotEmpty() || stack.selectedFolders.isNotEmpty()) {
                if (stack.selectedFiles.size == 1) {
                    viewState.onCreateDownloadFile(stack.selectedFiles[0].title)
                } else {
                    viewState.onCreateDownloadFile(ApiContract.DOWNLOAD_ZIP_NAME)
                }
            } else if (itemClicked is CloudFile) {
                viewState.onCreateDownloadFile((itemClicked as CloudFile).title)
            } else if (itemClicked is CloudFolder) {
                viewState.onCreateDownloadFile(ApiContract.DOWNLOAD_ZIP_NAME)
            }
        }
    }

    open fun download(downloadTo: Uri) {
        if (preferenceTool.uploadWifiState && !isWifiEnable(context)) {
            viewState.onSnackBar(context.getString(R.string.upload_error_wifi))
        }
        else if (modelExplorerStack?.countSelectedItems!! > 0) {
            downloadSelected(downloadTo)
        } else {
            itemClicked?.let { item ->
                when (item) {
                    is CloudFolder -> bulkDownload(null, listOf(item), downloadTo)
                    is CloudFile -> startDownloadWork(downloadTo, item.id, item.viewUrl, null)
                }
            }
        }
    }

    private fun downloadSelected(downloadTo: Uri) {
        val files = modelExplorerStack?.selectedFiles
        val folders = modelExplorerStack?.selectedFolders

        if (fileProvider is WebDavFileProvider && folders?.isNotEmpty() == true) {
            viewState.onError(context.getString(R.string.download_manager_folders_download))
        } else {
            bulkDownload(files, folders, downloadTo)
            deselectAll()
        }
    }

    @SuppressLint("MissingPermission")
    private fun bulkDownload(files: List<CloudFile>?, folders: List<CloudFolder>?, downloadTo: Uri) {
        val filesIds = files?.map { it.id } ?: listOf()
        val foldersIds = folders?.map { it.id } ?: listOf()

        if (filesIds.size > 1 || foldersIds.isNotEmpty()) {
            startDownloadWork(downloadTo, null, null, RequestDownload().also {
                it.filesIds = filesIds
                it.foldersIds = foldersIds
            })
        } else {
            startDownloadWork(downloadTo, filesIds[0], files?.get(0)?.viewUrl, null)
        }
    }

    private fun startDownloadWork(to: Uri, id: String?, url: String?, requestDownload: RequestDownload?) {
        val workData = Data.Builder()
            .putString(DownloadWork.FILE_ID_KEY, id)
            .putString(DownloadWork.URL_KEY, url)
            .putString(DownloadWork.FILE_URI_KEY, to.toString())
            .putString(DownloadWork.REQUEST_DOWNLOAD, Gson().toJson(requestDownload))
            .build()

        val request = OneTimeWorkRequest.Builder(DownloadWork::class.java)
            .setInputData(workData)
            .build()

        downloadManager.enqueue(request)
    }

    fun cancelDownload() {
        if (downloadDisposable?.isDisposed == false) {
            downloadDisposable?.dispose()
        }
    }

    open fun upload(uri: Uri?, uris: ClipData?) {
        if (preferenceTool.uploadWifiState && !isWifiEnable(context)) {
            viewState.onSnackBar(context.getString(R.string.upload_error_wifi))
        } else {
            modelExplorerStack?.currentId?.let { id ->
                val uriList = mutableListOf<Uri>().apply {
                    if (uri != null) {
                        uploadUri = uri
                        add(uri)
                    } else if (uris != null) {
                        for (i in 0 until uris.itemCount) {
                            add(uris.getItemAt(i).uri)
                        }
                    }
                }

                if (uriList.size > 20) {
                    viewState.onError(context.getString(R.string.upload_manager_error_number_files))
                } else {
                    addUploadFiles(uriList, id)
                }
            }
        }

    }

    open fun uploadToMy(uri: Uri) {
        // Stub
    }

    private fun addUploadFiles(uriList: List<Uri>, id: String) {
        val uploadFiles = mutableListOf<UploadFile>()
        for (uri in uriList) {
            if (getSize(context, uri) > FileUtils.STRICT_SIZE) {
                viewState.onSnackBar(context.getString(R.string.upload_manager_error_file_size))
                continue
            }
            uploadFiles.add(UploadFile().apply {
                progress = 0
                folderId = id
                name = getName(context, uri)
                size = setSize(uri)
                setUri(uri)
                setId(uri.path)
            })
        }

        if (uploadFiles.isNotEmpty()) {
            putNewUploadFiles(id, ArrayList(uploadFiles))
            for (uri in uploadFiles) {
                val workData = Data.Builder()
                    .putString(UploadWork.TAG_UPLOAD_FILES, uri.uri.toString())
                    .putString(UploadWork.ACTION_UPLOAD_MY, UploadWork.ACTION_UPLOAD)
                    .putString(UploadWork.TAG_FOLDER_ID, id)
                    .build()
                startUpload(workData)
            }

            if (modelExplorerStack?.last()?.itemsCount == 0) {
                refresh()
            } else {
                viewState.onAddUploadsFile(uploadFiles)
            }
        }
    }

    fun startUpload(data: Data) {
        data.getString(UploadWork.TAG_UPLOAD_FILES)?.let { tag ->
            val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
                .addTag(tag)
                .setInputData(data)
                .build()
            downloadManager.enqueue(request)
        }
    }

    private fun setSize(uri: Uri): String {
        return getFormattedSize(context, getSize(context, uri))
    }

    fun cancelUpload() {
        if (uploadDisposable?.isDisposed == false) {
            uploadDisposable?.dispose()
        } else if (downloadDisposable?.isDisposed == false) {
            downloadDisposable?.dispose()
        }
    }

    protected fun addFile(file: CloudFile?) {
        modelExplorerStack?.let { stack ->
            file?.isJustCreated = true
            stack.addFileFirst(file)
            viewState.onDocsGet(getListWithHeaders(stack.last(), true))
        }
    }

    private fun addFolder(folder: CloudFolder) {
        modelExplorerStack?.let { stack ->
            folder.isJustCreated = true
            stack.addFolderFirst(folder)
            viewState.onDocsGet(getListWithHeaders(stack.last(), true))
        }
    }

    fun addFolderAndOpen(folder: CloudFolder?, position: Int) {
        folder?.let {
            addFolder(folder)
            openFolder(folder.id, position)
        }
    }

    /**
     * ==============================================================================================
     * Common methods
     * ==============================================================================================
     * */

    protected open fun getArgs(filteringValue: String?): Map<String, String> {
        return TreeMap<String, String>().also { map ->
            map[ApiContract.Parameters.ARG_COUNT] = ITEMS_PER_PAGE.toString()
            map[ApiContract.Parameters.ARG_SORT_BY] = preferenceTool.sortBy ?: ""
            map[ApiContract.Parameters.ARG_SORT_ORDER] = preferenceTool.sortOrder ?: ""

            filteringValue?.let { value ->
                map[ApiContract.Parameters.ARG_FILTER_BY] = ApiContract.Parameters.VAL_FILTER_BY
                map[ApiContract.Parameters.ARG_FILTER_OP] = ApiContract.Parameters.VAL_FILTER_OP_CONTAINS
                map[ApiContract.Parameters.ARG_FILTER_VALUE] = value
            }
        }
    }

    private fun cancelGetRequests() {
        disposable.clear()
    }

    fun cancelSingleOperationsRequests() {
        disposable.clear()
    }

    fun resetDatesHeaders() {
        isFolderHeader = false
        isFileHeader = false
        isCreatedHeader = false
        isTodayHeader = false
        isYesterdayHeader = false
        isWeekHeader = false
        isMonthHeader = false
        isYearHeader = false
        isMoreYearHeader = false
    }

    /**
     * Batch operations
     * */

    fun moveSelected() {
        modelExplorerStack?.let { stack ->
            if (stack.countSelectedItems > 0) {
                stack.clone()?.let { clonedStack ->
                    clonedStack.removeUnselected()
                    viewState.onBatchMove(clonedStack.explorer)
                    getBackStack()
                }
            } else {
                viewState.onError(context.getString(R.string.operation_empty_lists_data))
            }
        }
    }

    fun moveContext() {
        modelExplorerStack?.last()?.clone()?.let { explorer ->
            viewState.onBatchMove(getBatchExplorer(explorer))
        }
    }

    open fun copySelected() {
        modelExplorerStack?.let { stack ->
            if (stack.countSelectedItems > 0) {
                stack.clone()?.let { clonedStack ->
                    clonedStack.removeUnselected()
                    viewState.onBatchCopy(clonedStack.explorer)
                }
            }
            viewState.onError(context.getString(R.string.operation_empty_lists_data))
        }
    }

    fun copyContext() {
        modelExplorerStack?.last()?.clone()?.let { explorer ->
            viewState.onBatchCopy(getBatchExplorer(explorer))
        }
    }

    private fun getBatchExplorer(explorer: Explorer): Explorer {
        return explorer.also {
            it.count = 1
            it.total = 1
            itemClicked?.let { item ->
                when (item) {
                    is CloudFolder -> {
                        it.folders = listOf(item.clone().also { folder -> folder.isSelected = true })
                        it.files.clear()
                    }
                    is CloudFile -> {
                        it.files = listOf(item.clone().also { file -> file.isSelected = true })
                        it.folders.clear()
                    }
                }
            }
        }
    }

    /**
     *  Get common list with headers
     * */

    protected fun getListWithHeaders(explorer: Explorer?, isResetHeaders: Boolean): List<Entity> {
        explorer?.let {
            modelExplorerStack?.let { stack ->
                val entityList: MutableList<Entity> = mutableListOf()

                // Reset headers, when new list
                if (isResetHeaders) {
                    resetDatesHeaders()
                }

                getUploadFiles(stack.currentId)?.let { uploadFiles ->
                    if (uploadFiles.size != 0) {
                        entityList.add(Header(context.getString(R.string.upload_manager_progress_title)))
                        entityList.addAll(uploadFiles)
                    }
                }

                // Set folders headers
                if (explorer.folders.isNotEmpty()) {
                    if (!isFolderHeader) {
                        isFolderHeader = true
                        entityList.add(Header(context.getString(R.string.list_headers_folder)))
                    }
                    entityList.addAll(explorer.folders)
                }

                // Set files headers
                if (explorer.files.isNotEmpty() && !isFoldersMode) {
                    val sortBy = preferenceTool.sortBy
                    val sortOrder = preferenceTool.sortOrder
                    val fileList = explorer.files

                    if (ApiContract.Parameters.VAL_SORT_BY_UPDATED == sortBy) { // For date sort add times headers
                        val todayMs = todayMs
                        val yesterdayMs = yesterdayMs
                        val weekMs = weekMs
                        val monthMs = monthMs
                        val yearMs = yearMs
                        var itemMs: Long

                        // Set time headers
                        fileList.sortWith { o1: CloudFile, o2: CloudFile -> o1.updated.compareTo(o2.updated) }

                        if (sortOrder == ApiContract.Parameters.VAL_SORT_ORDER_DESC) {
                            fileList.reverse()
                        }

                        for (item in fileList) {
                            itemMs = item.updated.time

                            // Check created property
                            if (item.isJustCreated) {
                                if (!isCreatedHeader) {
                                    isCreatedHeader = true
                                    entityList.add(Header(context.getString(R.string.list_headers_created)))
                                }
                            } else {

                                // Check time intervals
                                if (itemMs >= todayMs) {
                                    if (!isTodayHeader) {
                                        isTodayHeader = true
                                        entityList.add(Header(context.getString(R.string.list_headers_today)))
                                    }
                                } else if (itemMs in (yesterdayMs + 1) until todayMs) {
                                    if (!isYesterdayHeader) {
                                        isYesterdayHeader = true
                                        entityList.add(Header(context.getString(R.string.list_headers_yesterday)))
                                    }
                                } else if (itemMs in (weekMs + 1) until yesterdayMs) {
                                    if (!isWeekHeader) {
                                        isWeekHeader = true
                                        entityList.add(Header(context.getString(R.string.list_headers_week)))
                                    }
                                } else if (itemMs in (monthMs + 1) until weekMs) {
                                    if (!isMonthHeader) {
                                        isMonthHeader = true
                                        entityList.add(Header(context.getString(R.string.list_headers_month)))
                                    }
                                } else if (itemMs in (yearMs + 1) until monthMs) {
                                    if (!isYearHeader) {
                                        isYearHeader = true
                                        entityList.add(Header(context.getString(R.string.list_headers_year)))
                                    }
                                } else if (itemMs < yearMs) {
                                    if (!isMoreYearHeader) {
                                        isMoreYearHeader = true
                                        entityList.add(Header(context.getString(R.string.list_headers_more_year)))
                                    }
                                }
                            }
                            entityList.add(item)
                        }
                    } else {
                        if (!isFileHeader) {
                            isFileHeader = true
                            entityList.add(Header(context.getString(R.string.list_headers_files)))
                        }
                        entityList.addAll(fileList)
                    }
                }

                if (isFilteringMode) {
                    setPlaceholderType(if (entityList.isEmpty()) PlaceholderViews.Type.SEARCH else PlaceholderViews.Type.NONE)
                } else {
                    setPlaceholderType(if (entityList.isEmpty()) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                }

                return entityList
            }
        }
        return emptyList()
    }

    /**
     * ==============================================================================================
     * States methods
     * ==============================================================================================
     * */

    fun initViews() {
        setPlaceholderType(placeholderViewType)
        modelExplorerStack?.let { stack ->
            if (!isAccessDenied) {
                viewState.onDocsGet(getListWithHeaders(stack.last(), true))
            }
            refresh()
            updateOperationStack(stack.currentId)
        }
    }

    fun initMenu() {
        if (isSelectionMode) {
            viewState.onStateMenuSelection()
        } else {
            viewState.onStateMenuDefault(
                preferenceTool.sortBy ?: "",
                preferenceTool.sortOrder.equals(ApiContract.Parameters.VAL_SORT_ORDER_ASC, ignoreCase = true)
            )
        }
    }

    fun initMenuSearch() {
        if (isFilteringMode && !isSelectionMode) {
            viewState.onStateUpdateFilter(true, filteringValue)
        }
    }

    fun initMenuState() {
        viewState.onStateMenuEnabled(modelExplorerStack?.isListEmpty == false)
    }

    fun getBackStack(): Boolean {
        cancelGetRequests()
        when {
            isSelectionMode -> {
                setSelection(false)
                updateViewsState()
                return true
            }
            isFilteringMode -> {
                setFiltering(false)
                if (modelExplorerStack?.isStackFilter!!) {
                    popBackStack()
                }
                updateViewsState()
                return true
            }
            else -> {
                popBackStack()
                updateViewsState()
                return !modelExplorerStack?.isStackEmpty!!
            }
        }
    }

    private fun popBackStack() {
        modelExplorerStack?.previous()?.let {
            val entities = getListWithHeaders(modelExplorerStack?.last(), true)
            setPlaceholderType(if (modelExplorerStack?.isListEmpty!!) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
            viewState.onDocsGet(entities)
            viewState.onScrollToPosition(modelExplorerStack?.listPosition!!)
        }
    }

    private fun updateOperationStack(folderId: String?) {
        modelExplorerStack?.rootFolderType?.let { type ->
            operationsState.getOperations(type, folderId)
                .find { it.operationType == OperationsState.OperationType.INSERT}?.let { refresh() }
        }
    }

    /**
     * Reset mods
     * */

    private fun resetMods() {
        setFiltering(false)
        setSelection(false)
    }

    /**
     * Reset/Set views to filtering mode
     * */

    fun setFiltering(isFiltering: Boolean) {
        isSubmitted = false
        if (isFilteringMode != isFiltering) {
            isFilteringMode = isFiltering
            if (!isFiltering) { filteringValue = "" }
            viewState.onStateUpdateFilter(isFiltering, filteringValue)
        }
    }

    /**
     * Reset/Set model to selection mode
     * */

    fun setSelection(isSelection: Boolean) {
        if (isSelectionMode != isSelection) {
            isSelectionMode = isSelection
            if (!isSelection) {
                modelExplorerStack?.setSelection(false)
            }
            viewState.onStateUpdateSelection(isSelection)
        }
    }

    fun setSelectionAll() {
        setSelection(true)
        selectAll()
    }

    fun selectAll() {
        viewState.onItemsSelection(modelExplorerStack?.setSelection(true).toString())
        viewState.onStateUpdateSelection(true)
    }

    fun deselectAll() {
        viewState.onItemsSelection(modelExplorerStack?.setSelection(false).toString())
        viewState.onStateUpdateSelection(false)
        getBackStack()
    }

    val isSelectedAll: Boolean
        get() = modelExplorerStack?.countSelectedItems == modelExplorerStack?.totalCount

    /**
     * Get clicked item and do action with current state
     * */

    protected fun onClickEvent(item: Item?, position: Int) {
        itemClickedPosition = position
        itemClicked = modelExplorerStack?.getItemById(item)
    }

    open fun onItemClick(item: Item, position: Int) {
        onClickEvent(item, position)
        isContextClick = false
        itemClicked?.let { itemClicked ->
            if (isSelectionMode) {
                modelExplorerStack?.setSelectById(item, !itemClicked.isSelected)
                if (!isSelectedItemsEmpty) {
                    viewState.onStateUpdateSelection(true)
                    viewState.onItemSelected(position, modelExplorerStack?.countSelectedItems.toString())
                }
            } else {
                if (itemClicked is CloudFolder) {
                    openFolder(itemClicked.getId(), position)
                } else if (itemClicked is CloudFile) {
                    getFileInfo()
                }
            }
        }
    }

    protected val isSelectedItemsEmpty: Boolean
        get() = modelExplorerStack?.let { stack ->
            if (stack.countSelectedItems <= 0) {
                getBackStack()
                true
            } else {
                false
            }
        } ?: false

    fun getListMedia(clickedId: String?): Explorer {
        return modelExplorerStack?.last()?.let { explorer ->
            Explorer().apply {
                folders = listOf()
                files = explorer.files
                    .filter { isImage(it.fileExst) || isVideoSupport(it.fileExst) }
                    .onEach { it.isClicked = it.id.equals(clickedId, ignoreCase = true) }
            }
        } ?: Explorer()
    }

    fun openFolder(id: String?, position: Int) {
        modelExplorerStack?.listPosition = position
        viewState.onSwipeEnable(true)
        getItemsById(id)
    }

    /**
     * Get clicked context item and save it
     * */

    protected val isPdf: Boolean
        get() {
            itemClicked?.let { item ->
                if (item is CloudFile) {
                    return getExtension(item.fileExst) == StringUtils.Extension.PDF
                }
            }
            return false
        }

    protected fun getIconContext(ext: String): Int {
        return when (getExtension(ext)) {
            StringUtils.Extension.DOC -> R.drawable.ic_type_text_document
            StringUtils.Extension.SHEET -> R.drawable.ic_type_spreadsheet
            StringUtils.Extension.PRESENTATION -> R.drawable.ic_type_presentation
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF -> R.drawable.ic_type_image
            StringUtils.Extension.HTML, StringUtils.Extension.EBOOK, StringUtils.Extension.PDF -> R.drawable.ic_type_pdf
            StringUtils.Extension.VIDEO_SUPPORT -> R.drawable.ic_type_video
            StringUtils.Extension.UNKNOWN -> R.drawable.ic_type_file
            else -> R.drawable.ic_type_folder
        }
    }

    fun uploadPermission() {
        viewState.onFileUploadPermission()
    }

    val itemTitle: String
        get() = itemClicked?.let { item ->
            getNameWithoutExtension(item.title)
        } ?: ""

    fun setItemsShared(isShared: Boolean) {
        itemClicked?.shared = isShared
    }

    /**
     * Check on empty stack
     * */

    fun checkBackStack() {
        if (isBackStackEmpty) {
            viewState.onStateEmptyBackStack()
        } else {
            initViews()
        }
    }

    /**
     * Dialogs templates
     * */
    protected fun showDialogWaiting(tag: String?) {
        viewState.onDialogWaiting(context.getString(R.string.dialogs_wait_title), tag)
    }

    protected fun showDialogProgress(isHideButtons: Boolean, tag: String?) {
        viewState.onDialogProgress(context.getString(R.string.dialogs_wait_title), isHideButtons, tag)
    }

    /**
     * On batch operation
     * */

    protected fun onBatchOperations() {
        viewState.onDialogClose()
        viewState.onSnackBar(context.getString(R.string.operation_complete_message))
        viewState.onDocsBatchOperation()
    }

    private fun onFileDeleteProtected() {
        viewState.onDialogClose()
        if (isMultipleDelete) {
            viewState.onSnackBar(context.getString(R.string.operation_complete_message)
                    + context.getString(R.string.operation_delete_multiple))
        } else {
            viewState.onSnackBar(context.getString(R.string.operation_delete_impossible))
        }
        viewState.onDocsBatchOperation()
    }

    val isRoot: Boolean
        get() = modelExplorerStack?.isRoot == true

    private val isBackStackEmpty: Boolean
        get() = modelExplorerStack?.isStackEmpty == true

    protected val currentTitle: String
        get() = modelExplorerStack?.currentTitle ?: ""

    protected val itemClickedTitle: String
        get() = itemClicked?.title ?: ""

    protected val isClickedItemFile: Boolean
        get() = itemClicked is CloudFile

    protected val isClickedItemDocs: Boolean
        get() = itemClicked?.let { item -> isClickedItemFile && isDocument((item as CloudFile).fileExst) } == true

    protected val itemClickedDate: Date?
        get() = itemClicked?.updated

    protected fun setPlaceholderType(placeholderType: PlaceholderViews.Type?) {
        this.placeholderViewType = placeholderType
        if (isFoldersMode && placeholderType == PlaceholderViews.Type.EMPTY) {
            this.placeholderViewType = PlaceholderViews.Type.SUBFOLDER
        }
        viewState.onPlaceholder(this.placeholderViewType)
    }

    fun setOperationExplorer(explorer: Explorer) {
        operationStack = ExplorerStackMap(explorer)
    }

    private fun changeContent(explorer: Explorer): Explorer {
        val fileList = explorer.files.listIterator()
        while (fileList.hasNext()) {
            val file = fileList.next()
            val currentFolder = CloudFolder()
            currentFolder.id = modelExplorerStack?.currentId
            if (file.fileType.isEmpty() && file.fileExst.isEmpty()) {
                fileList.remove()
                fileProvider?.let { provider ->
                    disposable.add(provider.delete(listOf(file), currentFolder)
                            .subscribe({ modelExplorerStack?.refreshStack(explorer) }, this::fetchError))
                }
            }
        }
        return explorer
    }

    private fun isContainsInPath(folderId: String): Boolean {
         modelExplorerStack?.path?.let { foldersPath ->
            for (item in foldersPath) {
                if (equals(item, folderId)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isContainsPathInPath(path: List<String>): Boolean {
        for (item in path) {
            if (isContainsInPath(item)) {
                return true
            }
        }
        return false
    }

    @SuppressLint("StringFormatInvalid", "StringFormatMatches")
    protected open fun fetchError(throwable: Throwable) {
        if (throwable.message == ProviderError.INTERRUPT) {
            checkStatusOperation()
        } else if (throwable.message == ProviderError.FORBIDDEN) {
            viewState.onError(context.getString(R.string.dialogs_edit_forbidden_symbols))
        } else if (!isOnline(context) && throwable is UnknownHostException) {
            viewState.onError(context.getString(R.string.errors_connection_error))
        } else {
            viewState.onDialogClose()
            if (throwable is HttpException) {
                throwable.response()?.let { response ->
                    onErrorHandle(response.errorBody(), response.code())
                    if (response.code() == 412) {
                        viewState.onError(context.getString(R.string.operation_move_file_existing, throwable.suppressed[0].message))
                    } else if (response.code() >= ApiContract.HttpCodes.CLIENT_ERROR && response.code() < ApiContract.HttpCodes.SERVER_ERROR) {
                        if (!isRoot) {
                            modelExplorerStack?.previous()
                            getItemsById(modelExplorerStack?.currentId)
                        }
                    } else if (response.code() >= ApiContract.HttpCodes.SERVER_ERROR) {
                        setPlaceholderType(PlaceholderViews.Type.ACCESS)
                        viewState.onError(throwable.message)
                    } else if (response.code() == 500) {
                        viewState.onSnackBar(context.getString(R.string.list_context_rename_success))
                        refresh()
                    }
                }
            } else {
                onFailureHandle(throwable)
            }
        }
    }

    protected fun onErrorHandle(responseBody: ResponseBody?, responseCode: Int) {
        // Error values from server
        var errorMessage: String? = null
        var responseMessage: String? = null

        // Get error message
        try {
            responseMessage = responseBody?.string()
        } catch (e: Exception) {
            // No need handle
        }

        // Get Json error message
        responseMessage?.let {
            getJsonObject(responseMessage)?.let { jsonObject ->
                try {
                    //errorCode = jsonObject.getInt(KEY_ERROR_CODE);
                    errorMessage = jsonObject.getJSONObject(KEY_ERROR_INFO).getString(KEY_ERROR_INFO_MESSAGE)
                } catch (e: JSONException) {
                    Log.e(TAG, "onErrorHandle()", e)
                    addCrash(e)
                }
            }
        }

        // Delete this block -- BEGIN --
        // Callback error
        if (responseCode >= ApiContract.HttpCodes.REDIRECTION && responseCode < ApiContract.HttpCodes.CLIENT_ERROR) {
            viewState.onError(context.getString(R.string.errors_redirect_error) + responseCode)
        } else if (responseCode >= ApiContract.HttpCodes.CLIENT_ERROR &&
            responseCode < ApiContract.HttpCodes.SERVER_ERROR) {

            // Add here new message for common errors
            when (responseCode) {
                ApiContract.HttpCodes.CLIENT_UNAUTHORIZED -> viewState.onError(context.getString(R.string.errors_client_unauthorized))
                ApiContract.HttpCodes.CLIENT_FORBIDDEN -> {
                    if (errorMessage?.contains(ApiContract.Errors.DISK_SPACE_QUOTA) == true) {
                        viewState.onError(errorMessage)
                    } else {
                        viewState.onError(context.getString(R.string.errors_client_forbidden))
                    }
                }
                ApiContract.HttpCodes.CLIENT_NOT_FOUND -> {
                    viewState.onError(context.getString(R.string.errors_client_host_not_found))
                }
                ApiContract.HttpCodes.CLIENT_PAYMENT_REQUIRED -> {
                    viewState.onError(context.getString(R.string.errors_client_payment_required))
                }
                else -> viewState.onError(context.getString(R.string.errors_client_error) + responseCode)
            }
        } else if (responseCode >= ApiContract.HttpCodes.SERVER_ERROR) {
            // Add here new message for common errors
            if (errorMessage?.contains(ApiContract.Errors.AUTH) == true) {
                viewState.onError(context.getString(R.string.errors_server_auth_error))
            } else {
                viewState.onError(context.getString(R.string.errors_server_error) + responseCode)
            }
        } // Delete this block -- END --

//        // Uncomment this block, after added translation to server
//        // Callback error
//        if (errorMessage == null) {
//            if (responseCode >= Api.HttpCodes.REDIRECTION && responseCode < Api.HttpCodes.CLIENT_ERROR) {
//                getViewState().onError(mContext.getString(R.string.errors_redirect_error) + responseCode);
//            } else if (responseCode >= Api.HttpCodes.CLIENT_ERROR && responseCode < Api.HttpCodes.SERVER_ERROR) {
//                getViewState().onError(mContext.getString(R.string.errors_client_error) + responseCode);
//            } else if (responseCode >= Api.HttpCodes.SERVER_ERROR) {
//                getViewState().onError(mContext.getString(R.string.errors_server_error) + responseCode);
//            }
//        } else {
//            getViewState().onError(errorMessage);
//        }
    }

    /**
     * On fail connection
     * Add new handle of failure error here
     * */

    private fun onFailureHandle(throwable: Throwable?) {
        when (throwable) {
            is NoConnectivityException -> {
                viewState.onError(context.getString(R.string.errors_connection_error))
                onNetworkHandle()
            }
            is UnknownHostException -> {
                viewState.onError(context.getString(R.string.errors_unknown_host_error))
            }
            is SSLHandshakeException -> {
                viewState.onError(context.getString(R.string.errors_ssl_error))
            }
            else -> {
                throwable?.let {
                    addCrash(BasePresenter::class.java.simpleName + " - method - onFailureHandle()")
                    addCrash(it)
                }

                viewState.onError(context.getString(R.string.errors_unknown_error))
            }
        }
    }

    private fun onNetworkHandle() {
        context.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (isOnline(context)) {
                        refresh()
                        context.unregisterReceiver(this)
                    }
                }
            }, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    private fun checkStatusOperation() {
        onBatchOperations()
    }

    val stack: Explorer?
        get() = modelExplorerStack?.last()

    private fun setAccess(explorer: Explorer?): Explorer {
        return explorer?.also {
            explorer.files.map { file ->
                file.also { it.access = "0" }
            }
            explorer.folders.map { folder ->
                folder.also { it.access = "0" }
            }
        } ?: Explorer()
    }

    fun setAccessDenied() {
        isAccessDenied = true
        placeholderViewType = PlaceholderViews.Type.ACCESS
    }

    fun clearStack() {
        modelExplorerStack?.clear()
    }

    fun recreateStack() {
        modelExplorerStack = ModelExplorerStack()
    }

    abstract fun getNextList()

    abstract fun getFileInfo()

    abstract fun createDocs(title: String)

    abstract fun addRecent(file: CloudFile)

    abstract fun onContextClick(item: Item, position: Int, isTrash: Boolean)

    abstract fun onActionClick()

    protected abstract fun updateViewsState()

    companion object {

        val TAG = DocsBasePresenter::class.java.simpleName

        private const val KEY_ERROR_CODE = "statusCode"
        private const val KEY_ERROR_INFO = "error"
        private const val KEY_ERROR_INFO_MESSAGE = "message"

        /**
        * Tags for dialog action callback
        * */

        const val TAG_DIALOG_CONTEXT_RENAME = "TAG_DIALOG_CONTEXT_RENAME"
        const val TAG_DIALOG_CONTEXT_SHARE_DELETE = "TAG_DIALOG_CONTEXT_SHARE_DELETE"
        const val TAG_DIALOG_ACTION_SHEET = "TAG_DIALOG_ACTION_SHEET"
        const val TAG_DIALOG_ACTION_PRESENTATION = "TAG_DIALOG_ACTION_PRESENTATION"
        const val TAG_DIALOG_ACTION_DOC = "TAG_DIALOG_ACTION_DOC"
        const val TAG_DIALOG_ACTION_FOLDER = "TAG_DIALOG_ACTION_FOLDER"
        const val TAG_DIALOG_ACTION_REMOVE_SHARE = "TAG_DIALOG_ACTION_REMOVE_SHARE"
        const val TAG_DIALOG_DELETE_CONTEXT = "TAG_DIALOG_DELETE_CONTEXT"
        const val TAG_DIALOG_BATCH_DELETE_CONTEXT = "TAG_DIALOG_BATCH_DELETE_CONTEXT"
        const val TAG_DIALOG_BATCH_DELETE_SELECTED = "TAG_DIALOG_BATCH_DELETE_SELECTED"
        const val TAG_DIALOG_BATCH_EMPTY = "TAG_DIALOG_BATCH_EMPTY"
        const val TAG_DIALOG_BATCH_TERMINATE = "TAG_DIALOG_BATCH_TERMINATE"
        const val TAG_DIALOG_CANCEL_DOWNLOAD = "TAG_DIALOG_CANCEL_DOWNLOAD"
        const val TAG_DIALOG_CANCEL_UPLOAD = "TAG_DIALOG_CANCEL_UPLOAD"
        const val TAG_DIALOG_CANCEL_SINGLE_OPERATIONS = "TAG_DIALOG_CANCEL_SINGLE_OPERATIONS"
        const val TAG_DIALOG_CANCEL_BATCH_OPERATIONS = "TAG_DIALOG_CANCEL_BATCH_OPERATIONS"

        /**
         * Requests values
         * */

        private const val ITEMS_PER_PAGE = 25
        private const val FILTERING_DELAY = 500
    }
}