package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.database.datasource.RecentDataSource
import app.documents.core.model.cloud.Access
import app.documents.core.model.cloud.Recent
import app.documents.core.network.common.Result
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Current
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.UploadFile
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestDownload
import app.documents.core.providers.BaseFileProvider
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.FileOpenResult
import app.documents.core.providers.LocalFileProvider
import app.documents.core.providers.ProviderError
import app.documents.core.providers.ProviderError.Companion.throwInterruptException
import app.documents.core.providers.WebDavFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.FirebaseUtils.addCrash
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.managers.works.DownloadWork
import app.editors.manager.managers.works.UploadWork
import app.editors.manager.mvp.models.filter.FilterType
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.mvp.models.filter.joinToString
import app.editors.manager.mvp.models.list.RecentViaLink
import app.editors.manager.mvp.models.models.ExplorerStackMap
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.models.ui.StorageQuota
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.ui.views.custom.PlaceholderViews
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.EditType
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.NetworkUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.presenterScope
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.HttpException
import java.io.File
import java.net.UnknownHostException
import java.util.Date
import java.util.TreeMap
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

sealed class PickerMode {

    data object None : PickerMode()

    data object Folders : PickerMode()

    data object Ordering : PickerMode()

    sealed class Files(
        open val selectedIds: MutableList<String> = mutableListOf(),
        open val destFolderId: String
    ) : PickerMode() {

        data class PDFForm(override val destFolderId: String) : Files(destFolderId = destFolderId)

        data class Any(override val destFolderId: String) : Files(destFolderId = destFolderId)

        fun selectId(id: String) {
            if (id in selectedIds) {
                selectedIds.remove(id)
            } else {
                selectedIds.add(id)
            }
        }
    }
}

@InjectViewState
abstract class DocsBasePresenter<V : DocsBaseView, FP : BaseFileProvider> : MvpPresenter<V>(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var preferenceTool: PreferenceTool

    @Inject
    lateinit var operationsState: OperationsState

    @Inject
    lateinit var cloudDataSource: CloudDataSource

    @Inject
    lateinit var recentDataSource: RecentDataSource

    @Inject
    lateinit var fileProvider: FP

    /**
     * Handler for some common job
     * */

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Saved values
     * */

    protected var modelExplorerStack: ModelExplorerStack = ModelExplorerStack()
    protected var filteringValue: String = ""
    private var placeholderViewType: PlaceholderViews.Type = PlaceholderViews.Type.NONE
    protected var operationStack: ExplorerStackMap? = null
    private var uploadUri: Uri? = null
    private var sendingFile: File? = null

    protected var filters: Map<String, String> = mapOf()

    var destFolderId: String? = null
        protected set

    /**
     * SharedPreferences Settings
     * */

    val keepScreenOnSetting: Boolean
        get() = preferenceTool.keepScreenOn

    /**
     * Modes
     * */

    var pickerMode: PickerMode = PickerMode.None

    var isTrashMode = false

    var isSelectionMode = false

    var isFilteringMode = false
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

    var roomClicked: CloudFolder? = null
        private set

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
    protected var requestJob: Job? = null
    protected var openFileJob: Job? = null
    protected var batchDisposable: Disposable? = null
    protected var uploadDisposable: Disposable? = null
    protected var downloadDisposable: Disposable? = null
    private var sendDisposable: Disposable? = null
    private var isTerminate = false
    private var isAccessDenied = false

    /**
     * Download WorkManager
     */

    private var downloadManager = WorkManager.getInstance(App.getApp().applicationContext)
    private var isMultipleDelete = false

    protected var currentSectionType = ApiContract.SectionType.UNKNOWN

    val currentFolderAccess: Access
        get() = Access.get(modelExplorerStack.currentFolderAccess)

    val currentFolder: Current?
        get() = modelExplorerStack.last()?.current

    var isIndexing: Boolean = false
        get() {
            return (modelExplorerStack.last()?.pathParts?.find { it.id == roomClicked?.id } != null &&
                    roomClicked?.indexing == true) || currentFolder?.indexing == true || field
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceTool.KEY_IS_GRID_VIEW -> {
                viewState.onSetGridView(preferenceTool.isGridView)
            }
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        preferenceTool.registerChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceTool.unregisterChangeListener(this)
        disposable.clear()
        disposable.dispose()
    }

    protected suspend fun onFileOpenCollect(result: Result<FileOpenResult>) {
        when (result) {
            is Result.Error -> fetchError(result.exception)
            is Result.Success<FileOpenResult> -> onFileOpenCollect(result.result)
        }
    }

    protected open suspend fun onFileOpenCollect(result: FileOpenResult) {
        if (result !is FileOpenResult.Loading) viewState.onDialogClose()
        when (result) {
            is FileOpenResult.DownloadNotSupportedFile -> viewState.onFileDownloadPermission()
            is FileOpenResult.Loading -> showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
            is FileOpenResult.OpenLocally -> {
                openFileFromPortal(result.file, result.fileId, result.editType)
            }
            is FileOpenResult.OpenCloudMedia -> {
                viewState.onFileMedia(getListMedia(result.media), false)
            }
            else -> Unit
        }
    }

    private fun openFileFromPortal(file: File, fileId: String, editType: EditType) {
        viewState.onOpenLocalFile(
            CloudFile().apply {
                id = fileId
                webUrl = Uri.fromFile(file).toString()
                fileExst = StringUtils.getExtensionFromPath(file.absolutePath)
                title = file.name
                viewUrl = file.absolutePath
            },
            editType
        )
    }

    open fun getItemsById(id: String?) {
        id?.let {
            setPlaceholderType(PlaceholderViews.Type.LOAD)
            disposable.add(
                fileProvider.getFiles(id, getArgs(filteringValue).putFilters())
                    .doOnNext { it.filterType = preferenceTool.filter.type.filterVal }
                    .subscribe({ explorer: Explorer? -> loadSuccess(explorer) }, this::fetchError)
            )
        }
    }

    open fun refresh(onRefresh: () -> Unit = {}): Boolean {
        //        setPlaceholderType(PlaceholderViews.Type.LOAD)
        modelExplorerStack.currentId?.let { id ->
            disposable.add(
                fileProvider.getFiles(id, getArgs(filteringValue).putFilters())
                    .doOnNext { it.filterType = preferenceTool.filter.type.filterVal }
                    .flatMap { explorer ->
                        modelExplorerStack.refreshStack(explorer)
                        Observable.just(getListWithHeaders(modelExplorerStack.last(), true))
                    }
                    .subscribe({ explorer ->
                        updateViewsState()
                        viewState.onDocsRefresh(explorer)
                        onRefresh()
                    }, this::fetchError)
            )
            viewState.onSwipeEnable(true)
            return true
        }
        return false
    }

    open fun sortBy(sortValue: String): Boolean {
        val isRepeatedTap = preferenceTool.sortBy == sortValue
        preferenceTool.sortBy = sortValue
        if (isRepeatedTap) reverseSortOrder()
        return refresh()
    }

    fun reverseSortOrder() {
        if (preferenceTool.sortOrder == ApiContract.Parameters.VAL_SORT_ORDER_ASC) {
            preferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_DESC
        } else {
            preferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_ASC
        }
    }

    open fun filter(value: String) {
        if (isFilteringMode) {
            modelExplorerStack.currentId?.let { id ->
                filteringValue = value
                fileProvider.getFiles(id, getArgs(value).putFilters())
                    .doOnNext { it.filterType = preferenceTool.filter.type.filterVal }
                    .subscribe({ explorer ->
                        modelExplorerStack.setFilter(explorer)
                        setPlaceholderType(
                            if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.SEARCH else
                                PlaceholderViews.Type.NONE
                        )
                        viewState.onDocsFilter(getListWithHeaders(modelExplorerStack.last(), true))
                    }, this::fetchError)
            }
        }
    }

    /**
     * Change docs
     * */

    fun createFolder(title: String?) {
        //        preferenceTool.portal?.let {
        //            addAnalyticsCreateEntity(it, false, null)
        //        }

        modelExplorerStack.currentId?.let { id ->
            val requestCreate = RequestCreate().apply {
                this.title =
                    title?.takeIf { title.isNotEmpty() } ?: context.getString(R.string.dialogs_edit_create_docs)
            }

            disposable.add(
                fileProvider.createFolder(id, requestCreate)
                    .subscribe({ folder ->
                        setPlaceholderType(PlaceholderViews.Type.NONE)
                        viewState.onDialogClose()
                        addFolder(folder)
                    }, this::fetchError)
            )

            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        }
    }

    private fun renameFolder(id: Item, title: String) {
        modelExplorerStack.currentId?.let { currentId ->
            disposable.add(
                fileProvider.rename(id, title, null)
                    .flatMap { fileProvider.getFiles(currentId, getArgs(null)) }
                    .subscribe({ item ->
                        viewState.onDialogClose()
                        viewState.onSnackBar(context.getString(R.string.list_context_rename_success))
                        loadSuccess(item)
                    }, this::fetchError)
            )
        }
    }

    private fun renameFile(id: Item, title: String, version: Int) {
        modelExplorerStack.currentId?.let { currentId ->
            disposable.add(
                fileProvider.rename(id, title, version)
                    .flatMap { fileProvider.getFiles(currentId, getArgs(null)) }
                    .subscribe({ item ->
                        viewState.onDialogClose()
                        viewState.onSnackBar(context.getString(R.string.list_context_rename_success))
                        loadSuccess(item)
                    }, this::fetchError)
            )
        }
    }


    protected fun loadSuccess(explorer: Explorer?) {
        modelExplorerStack.addStack(explorer)
        updateViewsState()
        setPlaceholderType(if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
        modelExplorerStack.last()?.let { last -> viewState.onDocsGet(getListWithHeaders(last, true)) }

    }

    open fun deleteItems() {
        val items = mutableListOf<Item>().apply {
            if (modelExplorerStack.countSelectedItems > 0) {
                addAll(modelExplorerStack.selectedFiles)
                addAll(modelExplorerStack.selectedFolders)
            } else if (itemClicked != null) {
                modelExplorerStack.getItemById(itemClicked)?.let { item -> add(item) }
            }
        }

        showDialogProgress(true, TAG_DIALOG_CANCEL_BATCH_OPERATIONS)
            if (isRecentViaLinkSection()) {
                batchDisposable = (fileProvider as? CloudFileProvider)
                    ?.deleteRecent(items.map { it.id })
                    ?.subscribe({
                        viewState.onDialogProgress(100, 100)
                        if (modelExplorerStack.countSelectedItems > 0) {
                            modelExplorerStack.removeSelected()
                            getBackStack()
                        } else if (itemClicked != null) {
                            modelExplorerStack.removeItemById(itemClicked?.id)
                        }
                        resetDatesHeaders()
                        setPlaceholderType(if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                        viewState.onDialogClose()
                        viewState.onDeleteBatch(getListWithHeaders(modelExplorerStack.last(), true))
                    }, ::fetchError)
                return
            }

            batchDisposable = fileProvider.delete(items, null)
                .switchMap { status }
                .subscribe({ progress ->
                    viewState.onDialogProgress(100, progress ?: 0)
                }, this::fetchError) {

                    if (modelExplorerStack.countSelectedItems > 0) {
                        modelExplorerStack.removeSelected()
                        getBackStack()
                    } else if (itemClicked != null) {
                        modelExplorerStack.removeItemById(itemClicked?.id)
                    }

                    resetDatesHeaders()
                    setPlaceholderType(if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                    viewState.onDeleteBatch(getListWithHeaders(modelExplorerStack.last(), true))

                    if (isMultipleDelete) {
                        onFileDeleteProtected()
                        isMultipleDelete = false
                    } else {
                        viewState.onDialogClose()
                        viewState.onDeleteMessage(items.size)
                    }
        }
    }

    open fun delete(): Boolean {
        if (modelExplorerStack.countSelectedItems > 0) {
            if (isRecentViaLinkSection()) {
                deleteItems()
                return true
            }

            for (item in modelExplorerStack.selectedFiles) {
                isFileDeleteProtected(item)?.let { observable ->
                    disposable.add(
                        observable.subscribe({ isFileProtected ->
                            if (isFileProtected) {
                                isMultipleDelete = true
                                modelExplorerStack.setSelectById(item, false)
                            }
                        }, this::fetchError)
                    )
                }
            }
            viewState.onDialogDelete(
                modelExplorerStack.countSelectedItems,
                true,
                TAG_DIALOG_BATCH_DELETE_SELECTED
            )
        } else if (!isSelectionMode) {
            if (itemClicked is CloudFile) {
                disposable.add(
                    fileProvider.fileInfo(itemClicked)
                        .doOnError(::fetchError)
                        .doOnNext { file ->
                            if (file.isEditing) {
                                onFileDeleteProtected()
                            } else {
                                deleteItems()
                            }
                        }
                        .subscribe()
                )
            } else {
                deleteItems()
            }
        } else {
            viewState.onSnackBar(context.getString(R.string.operation_empty_lists_data))
        }
        return true
    }

    private fun isFileDeleteProtected(item: Item): Observable<Boolean>? {
        return Observable.just(fileProvider.fileInfo(item))
            .flatMap { response: Observable<CloudFile> ->
                response.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap { file: CloudFile ->
                        if (file.isEditing) {
                            Observable.just(true)
                        } else {
                            Observable.just(false)
                        }
                    }
            }
    }

    open fun move(): Boolean {
        modelExplorerStack.currentId?.also { destFolder ->
            destFolderId = destFolder
            operationStack?.let { stack ->
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
            val destination = CloudFolder().apply { id = destFolderId.orEmpty() }

            val items = mutableListOf<Item>().apply {
                addAll(operationStack.selectedFiles)
                addAll(operationStack.selectedFolders)
            }

            batchDisposable = fileProvider.transfer(items, destination, conflict, isMove, false)?.let { observable ->
                observable.switchMap { status }
                    .subscribe(
                        { progress: Int? -> viewState.onDialogProgress(100, progress ?: 0) },
                        this::fetchError
                    ) {
                        if (!operationStack.currentId.equals(destFolderId, ignoreCase = true)) {
                            operationStack.setSelectionAll(false)
                            operationStack.explorer?.also {
                                it.destFolderId = destFolderId.orEmpty()
                                operationsState.insert(modelExplorerStack.rootFolderType, it.takeIf {
                                    modelExplorerStack.rootFolderType == ApiContract.SectionType.CLOUD_USER
                                } ?: setAccess(it))
                            }
                        }
                        setPlaceholderType(if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                        onBatchOperations()
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
                    val response = fileProvider.getStatusOperation()?.response
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
        fileProvider.terminate()?.let { provider ->
            disposable.add(
                provider
                    .doOnSubscribe { showDialogProgress(true, TAG_DIALOG_BATCH_TERMINATE) }
                    .subscribe({
                        isTerminate = false
                        onBatchOperations()
                    }, this::fetchError)
            )
        }
    }

    open fun copy(): Boolean {
        modelExplorerStack.currentId?.also { destFolder ->
            destFolderId = destFolder
            operationStack?.let { stack ->
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
                modelExplorerStack.getItemById(item)?.let { stackItem ->
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
        if (modelExplorerStack.selectedFiles.isNotEmpty() || modelExplorerStack.selectedFolders.isNotEmpty()) {
            if (modelExplorerStack.selectedFiles.size == 1) {
                viewState.onCreateDownloadFile(modelExplorerStack.selectedFiles[0].title)
            } else {
                viewState.onCreateDownloadFile(ApiContract.DOWNLOAD_ZIP_NAME)
            }
        } else if (itemClicked is CloudFile) {
            viewState.onCreateDownloadFile((itemClicked as CloudFile).title)
        } else if (itemClicked is CloudFolder) {
            viewState.onCreateDownloadFile(ApiContract.DOWNLOAD_ZIP_NAME)
        }
    }

    open fun download(downloadTo: Uri) {
        if (preferenceTool.uploadWifiState && !NetworkUtils.isWifiEnable(context)) {
            viewState.onSnackBar(context.getString(R.string.upload_error_wifi))
        } else if (modelExplorerStack.countSelectedItems > 0) {
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
        bulkDownload(modelExplorerStack.selectedFiles, modelExplorerStack.selectedFolders, downloadTo)
        deselectAll()
    }

    @SuppressLint("MissingPermission")
    private fun bulkDownload(files: List<CloudFile>?, folders: List<CloudFolder>?, downloadTo: Uri) {
        val filesIds = files?.map { it.id } ?: listOf()
        val foldersIds = folders?.map { it.id } ?: listOf()

        if (fileProvider is WebDavFileProvider) {
            viewState.onError(context.getString(R.string.download_manager_folders_download))
        } else if (filesIds.size > 1 || foldersIds.isNotEmpty()) {
            startDownloadWork(downloadTo, null, null, RequestDownload()
                .also {
                    it.filesIds = filesIds
                    it.foldersIds = foldersIds
                }
            )
        } else {
            startDownloadWork(downloadTo, filesIds[0], files?.get(0)?.viewUrl, null)
        }
    }

    protected open fun startDownloadWork(
        to: Uri,
        id: String?,
        url: String?,
        requestDownload: RequestDownload?,
        worker: Class<out BaseDownloadWork> = DownloadWork::class.java
    ) {
        val workData = Data.Builder()
            .putString(BaseDownloadWork.FILE_ID_KEY, id)
            .putString(BaseDownloadWork.URL_KEY, url)
            .putString(BaseDownloadWork.FILE_URI_KEY, to.toString())
            .putString(BaseDownloadWork.REQUEST_DOWNLOAD, Gson().toJson(requestDownload))
            .build()

        val request = OneTimeWorkRequest.Builder(worker)
            .setInputData(workData)
            .build()

        downloadManager.enqueue(request)
    }

    fun cancelDownload() {
        if (downloadDisposable?.isDisposed == false) {
            downloadDisposable?.dispose()
        }
    }

    open fun upload(uri: Uri?, uris: List<Uri>?, tag: String? = null) {
        if (preferenceTool.uploadWifiState && !NetworkUtils.isWifiEnable(context)) {
            viewState.onSnackBar(context.getString(R.string.upload_error_wifi))
        } else {
            modelExplorerStack.currentId?.let { id ->
                val uriList = mutableListOf<Uri>().apply {
                    if (uri != null) {
                        uploadUri = uri
                        add(uri)
                    } else if (uris != null) {
                        for (i in uris.indices) {
                            add(uris[i])
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
            if (ContentResolverUtils.getSize(context, uri) > FileUtils.STRICT_SIZE) {
                viewState.onSnackBar(context.getString(R.string.upload_manager_error_file_size))
                continue
            }
            uploadFiles.add(UploadFile().apply {
                progress = 0
                folderId = id
                name = ContentResolverUtils.getName(context, uri)
                size = setSize(uri)
                this.uri = uri
                this.id = uri.path
            })
        }

        if (uploadFiles.isNotEmpty()) {
            UploadWork.putNewUploadFiles(id, ArrayList(uploadFiles))
            for (uri in uploadFiles) {
                val workData = Data.Builder()
                    .putString(UploadWork.TAG_UPLOAD_FILES, uri.uri.toString())
                    .putString(UploadWork.ACTION_UPLOAD_MY, UploadWork.ACTION_UPLOAD)
                    .putString(UploadWork.TAG_FOLDER_ID, id)
                    .build()
                startUpload(workData)
            }

            if (modelExplorerStack.last()?.itemsCount == 0) {
                refresh()
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
        return StringUtils.getFormattedSize(context, ContentResolverUtils.getSize(context, uri))
    }

    fun cancelUpload() {
        if (uploadDisposable?.isDisposed == false) {
            uploadDisposable?.dispose()
        } else if (downloadDisposable?.isDisposed == false) {
            downloadDisposable?.dispose()
        }
    }

    protected fun addFile(file: CloudFile?) {
        file?.isJustCreated = true
        modelExplorerStack.addFileFirst(file)
        viewState.onDocsGet(getListWithHeaders(modelExplorerStack.last(), true))
    }

    protected fun addFolder(folder: CloudFolder) {
        folder.isJustCreated = true
        modelExplorerStack.addFolderFirst(folder)
        viewState.onDocsGet(getListWithHeaders(modelExplorerStack.last(), true))
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

    protected open fun Map<String, String>.putFilters(): Map<String, String> {
        return plus(
            mutableMapOf<String, String>().apply {
                val filter = preferenceTool.filter
                if (ApiContract.SectionType.isRoom(currentSectionType) && isRoot) {
                    if (filter.roomType != RoomFilterType.None) {
                        put(ApiContract.Parameters.ARG_FILTER_BY_TYPE_ROOM, filter.roomType.filterVal.toString())
                    }
                    if (filter.provider != null) {
                        put(ApiContract.Parameters.ARG_FILTER_BY_PROVIDER_ROOM, filter.provider?.filterValue.orEmpty())
                    }
                    if (filter.tags.isNotEmpty()) {
                        put(ApiContract.Parameters.ARG_FILTER_BY_TAG_ROOM, filter.tags.joinToString())
                    }
                    put(ApiContract.Parameters.ARG_FILTER_BY_SUBJECT_ID, filter.author.id)
                } else {
                    put(ApiContract.Parameters.ARG_FILTER_BY_TYPE, filter.type.filterVal)
                    if (filter.type != FilterType.None || isFilteringMode && filteringValue.isNotEmpty()) {
                        put(ApiContract.Parameters.ARG_FILTER_SUBFOLDERS, (!filter.excludeSubfolder).toString())
                    }
                    if (App.getApp().accountOnline?.isPersonal() == false) {
                        put(ApiContract.Parameters.ARG_FILTER_BY_AUTHOR, filter.author.id)
                    }
                }
                putAll(filters)
            }
        )
    }

    private fun cancelGetRequests() {
        disposable.clear()
        requestJob?.cancel()
        openFileJob?.cancel()
    }

    fun cancelSingleOperationsRequests() {
        disposable.clear()
        requestJob?.cancel()
        openFileJob?.cancel()
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


    fun moveCopyOperation(operationsState: OperationsState.OperationType) {
        modelExplorerStack.last()?.clone()?.let { explorer ->
            viewState.onBatchMoveCopy(operationsState, getBatchExplorer(explorer))
        }
    }

    open fun moveCopySelected(operationsState: OperationsState.OperationType) {
        if (modelExplorerStack.countSelectedItems > 0) {
            modelExplorerStack.clone()?.let { clonedStack ->
                clonedStack.removeUnselected()
                viewState.onBatchMoveCopy(operationsState, clonedStack.explorer)
//                getBackStack()
            }
        } else {
            viewState.onError(context.getString(R.string.operation_empty_lists_data))
        }
    }

    //    fun moveContext() {
    //        modelExplorerStack.last()?.clone()?.let { explorer ->
    //            viewState.onBatchMove(getBatchExplorer(explorer))
    //        }
    //    }
    //
    //    open fun copySelected() {
    //        if (modelExplorerStack.countSelectedItems > 0) {
    //            modelExplorerStack.clone()?.let { clonedStack ->
    //                clonedStack.removeUnselected()
    //                viewState.onBatchCopy(clonedStack.explorer)
    //            }
    //        }
    //        viewState.onError(context.getString(R.string.operation_empty_lists_data))
    //    }
    //
    //    fun copyContext() {
    //        modelExplorerStack.last()?.clone()?.let { explorer ->
    //            viewState.onBatchCopy(getBatchExplorer(explorer))
    //        }
    //    }

    private fun getBatchExplorer(explorer: Explorer): Explorer {
        return explorer.also {
            it.count = 1
            it.total = 1
            itemClicked?.let { item ->
                when (item) {
                    is CloudFolder -> {
                        it.folders = mutableListOf(item.clone().also { folder -> folder.isSelected = true })
                        it.files.clear()
                    }
                    is CloudFile -> {
                        it.files = mutableListOf(item.clone().also { file -> file.isSelected = true })
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
        if (explorer == null) return emptyList()

        val entities = (explorer.folders + explorer.files).run {
            if (isIndexing) sortedBy(Item::index) else this
        }

        val placeholderType = if (entities.isEmpty()) {
            if (isFilteringMode) {
                PlaceholderViews.Type.SEARCH
            } else {
                if (ApiContract.SectionType.isRoom(currentSectionType) && isRoot) {
                    if (itemClicked?.security?.editRoom == true) {
                        PlaceholderViews.Type.NO_ROOMS
                    } else {
                        PlaceholderViews.Type.VISITOR_NO_ROOMS
                    }
                } else {
                    PlaceholderViews.Type.EMPTY
                }
            }
        } else {
            PlaceholderViews.Type.NONE
        }

        setPlaceholderType(placeholderType)

        return entities
    }

    /**
     * ==============================================================================================
     * States methods
     * ==============================================================================================
     * */

    fun initViews() {
        setPlaceholderType(placeholderViewType)
        if (!isAccessDenied) {
            viewState.onDocsGet(getListWithHeaders(modelExplorerStack.last(), true))
        }
        refresh()
        updateOperationStack(modelExplorerStack.currentId)
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
        viewState.onStateMenuEnabled(
            !modelExplorerStack.isListEmpty ||
                    ApiContract.SectionType.isRoom(currentSectionType)
        )
    }

    open fun getBackStack(): Boolean {
        cancelGetRequests()
        when {
            isSelectionMode && pickerMode !is PickerMode.Files -> {
                setSelection(false)
                updateViewsState()
                return true
            }

            isFilteringMode -> {
                setFiltering(false)
                if (modelExplorerStack.isStackFilter) {
                    popBackStack()
                }
                updateViewsState()
                return true
            }

            else -> {
                popBackStack()
                updateViewsState()
                return !modelExplorerStack.isStackEmpty
            }
        }
    }

    private fun popBackStack() {
        modelExplorerStack.previous()?.let {
            val entities = getListWithHeaders(modelExplorerStack.last(), true)
            setPlaceholderType(if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
            viewState.onDocsGet(entities)
            viewState.onScrollToPosition(modelExplorerStack.listPosition)
        }
    }

    fun popToRoot() {
        modelExplorerStack.popToRoot()?.let {
            val entities = getListWithHeaders(modelExplorerStack.last(), true)
            setPlaceholderType(if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
            viewState.onDocsGet(entities)
            viewState.onScrollToPosition(modelExplorerStack.listPosition)
            updateViewsState()
        }
    }

    private fun updateOperationStack(folderId: String?) {
        modelExplorerStack.rootFolderType.let { type ->
            operationsState.getOperations(type, folderId)
                .find { it.operationType == OperationsState.OperationType.INSERT }?.let { refresh() }
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
        if (isFilteringMode != isFiltering) {
            isFilteringMode = isFiltering
            if (!isFiltering) {
                filteringValue = ""
            }
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
                modelExplorerStack.setSelection(false)
            }
            viewState.onStateUpdateSelection(isSelection)
        }
    }

    fun setSelectionAll() {
        setSelection(true)
        selectAll()
    }

    fun selectAll() {
        viewState.onItemsSelection(modelExplorerStack.setSelection(true).toString())
        viewState.onStateUpdateSelection(true)
    }

    fun deselectAll() {
        viewState.onItemsSelection(modelExplorerStack.setSelection(false).toString())
        viewState.onStateUpdateSelection(false)
        getBackStack()
    }

    val isSelectedAll: Boolean
        get() = modelExplorerStack.countSelectedItems == modelExplorerStack.totalCount

    /**
     * Get clicked item and do action with current state
     * */

    fun onClickEvent(item: Item?, position: Int, isContext: Boolean = false) {
        itemClickedPosition = position
        itemClicked = if (item is RecentViaLink) item else modelExplorerStack.getItemById(item)
        if (item is CloudFolder && item.isRoom) roomClicked = item
        isContextClick = isContext
    }

    open fun onItemClick(item: Item, position: Int) {
        onClickEvent(item, position)
        isContextClick = false
        itemClicked?.let { itemClicked ->
            if (isSelectionMode) {
                modelExplorerStack.setSelectById(item, !itemClicked.isSelected)
                if (!isSelectedItemsEmpty) {
                    viewState.onStateUpdateSelection(true)
                    viewState.onItemSelected(position, modelExplorerStack.countSelectedItems.toString())
                }
            } else {
                if (itemClicked is CloudFolder) {
                    openFolder(itemClicked.id, position)
                } else if (itemClicked is CloudFile) {
                    openFile(itemClicked, EditType.Edit())
                }
            }
        }
    }

    protected val isSelectedItemsEmpty: Boolean
        get() = modelExplorerStack.let { stack ->
            if (stack.countSelectedItems <= 0) {
                getBackStack()
                true
            } else {
                false
            }
        }

    private fun getListMedia(mediaFile: CloudFile): Explorer {
        val lastExplorer = modelExplorerStack.last() ?: Explorer(files = mutableListOf(mediaFile))
        return Explorer().apply {
            folders = mutableListOf()
            files = lastExplorer.files
                .filter { StringUtils.isImage(it.fileExst) || StringUtils.isVideoSupport(it.fileExst) }
                .onEach { it.isClicked = it.id.equals(mediaFile.id, ignoreCase = true) }
                .toMutableList()
        }
    }

    open fun openFolder(id: String?, position: Int, roomType: Int? = null) {
        modelExplorerStack.listPosition = position
        viewState.onSwipeEnable(true)
        roomType?.let { roomClicked = CloudFolder().apply {
            this.id = id ?: ""
            this.roomType = it
        } }
        getItemsById(id)
    }

    /**
     * Get clicked context item and save it
     * */

    protected val isPdf: Boolean
        get() {
            itemClicked?.let { item ->
                if (item is CloudFile) {
                    return StringUtils.getExtension(item.fileExst) == StringUtils.Extension.PDF
                }
            }
            return false
        }

    protected fun getIconContext(ext: String): Int {
        return when (StringUtils.getExtension(ext)) {
            StringUtils.Extension.DOC -> R.drawable.ic_type_document_row
            StringUtils.Extension.SHEET -> R.drawable.ic_spreadsheet_secure_row
            StringUtils.Extension.PRESENTATION -> R.drawable.ic_presentation_secure_row
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF -> R.drawable.ic_type_picture_row
            StringUtils.Extension.HTML, StringUtils.Extension.EBOOK, StringUtils.Extension.PDF -> R.drawable.ic_type_pdf_row
            StringUtils.Extension.VIDEO_SUPPORT -> R.drawable.ic_type_video_row
            StringUtils.Extension.UNKNOWN -> R.drawable.ic_type_file
            else -> R.drawable.ic_type_folder
        }
    }

    fun uploadPermission(extension: String? = null) {
        viewState.onFileUploadPermission(extension)
    }

    fun showFileChooserFragment() {
        viewState.onPickCloudFile(currentFolder?.id)
    }

    val itemTitle: String
        get() = itemClicked?.let { item ->
            StringUtils.getNameWithoutExtension(item.title)
        } ?: ""

    val itemExtension: String
        get() = itemClicked?.let { item ->
            StringUtils.getExtensionFromPath(item.title)
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
            viewState.onSnackBar(
                context.getString(R.string.operation_complete_message)
                        + context.getString(R.string.operation_delete_multiple)
            )
        } else {
            viewState.onSnackBar(context.getString(R.string.operation_delete_impossible))
        }
        viewState.onDocsBatchOperation()
    }

    val isRoot: Boolean
        get() = if (ApiContract.SectionType.isRoom(modelExplorerStack.rootFolderType)) {
            modelExplorerStack.last()?.pathParts.orEmpty().size < 2
        } else {
            modelExplorerStack.isRoot
        }

    private val isBackStackEmpty: Boolean
        get() = modelExplorerStack.isStackEmpty

    protected val currentTitle: String
        get() = modelExplorerStack.currentTitle

    protected val itemClickedTitle: String
        get() = itemClicked?.title ?: ""

    protected val isClickedItemFile: Boolean
        get() = itemClicked is CloudFile

    protected val isClickedItemDocs: Boolean
        get() = itemClicked?.let { item -> isClickedItemFile && StringUtils.isDocument((item as CloudFile).fileExst) } == true

    protected val itemClickedDate: Date?
        get() = itemClicked?.updated

    protected fun setPlaceholderType(placeholderType: PlaceholderViews.Type) {
        this.placeholderViewType = placeholderType
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
            currentFolder.id = modelExplorerStack.currentId.orEmpty()
            if (file.fileType.isEmpty() && file.fileExst.isEmpty()) {
                fileList.remove()
                disposable.add(
                    fileProvider.delete(listOf(file), currentFolder)
                        .subscribe({ modelExplorerStack.refreshStack(explorer) }, this::fetchError)
                )
            }
        }
        return explorer
    }

    private fun isContainsInPath(folderId: String): Boolean {
        for (item in modelExplorerStack.path) {
            if (StringUtils.equals(item, folderId)) {
                return true
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

    private var photoUri: Uri? = null

    @SuppressLint("MissingPermission")
    fun createPhoto() {
        val photo = if (fileProvider is LocalFileProvider) {
            FileUtils.createFile(File(stack?.current?.id ?: ""), TimeUtils.fileTimeStamp, "png")
        } else {
            FileUtils.createFile(File(context.cacheDir.absolutePath), TimeUtils.fileTimeStamp, "png")
        }
        if (photo != null) {
            photoUri = ContentResolverUtils.getFileUri(context, photo).also { uri ->
                viewState.onShowCamera(uri)
            }
        }
    }

    fun deletePhoto() {
        photoUri?.let { uri ->
            context.contentResolver.delete(uri, null, null)
        }
    }

    @SuppressLint("StringFormatInvalid", "StringFormatMatches")
    protected open fun fetchError(throwable: Throwable) {
        if (throwable.message == ProviderError.INTERRUPT) {
            checkStatusOperation()
        } else if (throwable.message == ProviderError.FORBIDDEN) {
            viewState.onError(context.getString(R.string.dialogs_edit_forbidden_symbols))
        } else if (!NetworkUtils.isOnline(context) && throwable is UnknownHostException) {
            viewState.onError(context.getString(R.string.errors_connection_error))
        } else {
            viewState.onDialogClose()
            if (throwable is HttpException) {
                throwable.response()?.let { response ->
                    if (response.code() == ApiContract.HttpCodes.UNSUPPORTED_MEDIA_TYPE) {
                        //TODO Add Unsupported type localize message
                        viewState.onError(throwable.message)
                        return
                    }
                    onErrorHandle(response.errorBody(), response.code())
                    if (response.code() == 412) {
                        viewState.onError(
                            context.getString(
                                R.string.operation_move_file_existing,
                                throwable.suppressed[0].message
                            )
                        )
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
            StringUtils.getJsonObject(responseMessage)?.let { jsonObject ->
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
            responseCode < ApiContract.HttpCodes.SERVER_ERROR
        ) {

            // Add here new message for common errors
            when (responseCode) {
                ApiContract.HttpCodes.CLIENT_UNAUTHORIZED -> viewState.onError(context.getString(R.string.errors_client_unauthorized))
                ApiContract.HttpCodes.CLIENT_FORBIDDEN -> {
                    val message = errorMessage
                    if (message == null) {
                        viewState.onError(context.getString(R.string.errors_client_forbidden))
                        return
                    }
                    handleErrorMessage(message)
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

    protected open fun handleErrorMessage(message: String) {
        when {
            ApiContract.Errors.DISK_SPACE_QUOTA in message -> {
                viewState.onError(message)
            }
            ApiContract.Errors.STORAGE_NOT_AVAILABLE in message -> {
                viewState.onError(context.getString(R.string.room_storage_not_availabale))
                setPlaceholderType(PlaceholderViews.Type.NONE)
            }
            ApiContract.Errors.PINNED_ROOM_LIMIT in message -> {
                viewState.onDialogWarning(
                    context.getString(R.string.dialogs_warning_title),
                    context.getString(R.string.dialogs_warning_pinned_room_limit),
                    null
                )
            }
            ApiContract.Errors.EXCEED_ROOM_SPACE_QUOTA in message -> {
                val spaceQuota = StorageQuota.fromBytes(roomClicked?.quotaLimit).toString(context)
                viewState.onDialogWarning(
                    context.getString(R.string.dialogs_warning_title),
                    context.getString(R.string.dialogs_warning_room_space_quota_exceed, spaceQuota),
                    null
                )
            }
            else -> {
                viewState.onError(context.getString(R.string.errors_client_forbidden))
            }
        }
    }

    /**
     * On fail connection
     * Add new handle of failure error here
     * */

    private fun onFailureHandle(throwable: Throwable?) {
        when (throwable) {
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
                    if (NetworkUtils.isOnline(context)) {
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
        get() = modelExplorerStack.last()

    val folderId: String?
        get() = modelExplorerStack.currentId

    private fun setAccess(explorer: Explorer?): Explorer {
        return explorer?.apply {
            files = files
                .map { file -> file.also { it.access = Access.None } }
                .toMutableList()

            folders = folders
                .map { folder -> folder.also { it.access = Access.None } }
                .toMutableList()
        } ?: Explorer()
    }

    fun setAccessDenied() {
        isAccessDenied = true
        placeholderViewType = PlaceholderViews.Type.ACCESS
    }

    fun clearStack() {
        modelExplorerStack.clear()
    }

    fun recreateStack() {
        modelExplorerStack = ModelExplorerStack()
    }

    fun setSectionType(sectionType: Int) {
        currentSectionType = sectionType
    }

    fun getSectionType() = currentSectionType

    open fun sendCopy() {
        (itemClicked as? CloudFile)?.let { cloudFile ->
            context.accountOnline?.let { account ->
                sendDisposable = fileProvider.getCachedFile(context, cloudFile, account.accountName)
                    .doOnSubscribe { viewState.onDialogDownloadWaiting() }
                    .doOnError { viewState.onError(context.getString(R.string.errors_create_local_file)) }
                    .doOnSuccess { file ->
                        sendingFile = file
                        viewState.onDialogClose()
                        viewState.onSendCopy(file)
                    }
                    .subscribe()
            }
        }
    }

    fun removeSendingFile() {
        sendingFile?.delete()
        sendingFile = null
    }

    fun interruptFileSending() {
        sendDisposable?.dispose()
    }

    fun clearDisposable() {
        disposable.clear()
    }

    fun isRecentViaLinkSection(): Boolean {
        return modelExplorerStack.rootFolderType == ApiContract.SectionType.CLOUD_RECENT
    }

    fun isListEmpty(): Boolean {
        return modelExplorerStack.isListEmpty
    }

    fun isRoomFolder(): Boolean {
        return modelExplorerStack.last()?.current?.id == roomClicked?.id
    }

    fun setDestFolder(id: String) {
        destFolderId = id
    }

    fun setGridView(isGrid: Boolean) {
        preferenceTool.isGridView = isGrid
    }

    open fun openFile(editType: EditType, canBeShared: Boolean = false) {
        openFile(
            cloudFile = itemClicked as? CloudFile ?: return,
            editType = editType,
            canBeShared = canBeShared
        )
    }

    open fun openFile(cloudFile: CloudFile, editType: EditType, canBeShared: Boolean = false) {
        openFileJob?.cancel()
        openFileJob = presenterScope.launch {
            addToRecent(cloudFile)
            fileProvider.openFile(
                cloudFile = cloudFile,
                editType = editType,
                canBeShared = canBeShared
            ).collect(::onFileOpenCollect)
        }
    }

    open fun createDocs(title: String) {
        modelExplorerStack.currentId?.let { folderId ->
            disposable.add(
                fileProvider.createFile(
                    folderId = folderId,
                    title = title
                )
                    .doOnNext { file ->
                        viewState.onDialogClose()
                        openFile(file, EditType.Edit(false), true)
                    }
                    .doOnError {
                        viewState.onError(context.getString(R.string.errors_create_local_file))
                    }
                    .subscribe()
            )
        }
    }

    protected open suspend fun addToRecent(cloudFile: CloudFile) = withContext(Dispatchers.IO) {
        recentDataSource.insertOrUpdate(cloudFileToRecent(cloudFile))
    }

    protected open fun cloudFileToRecent(cloudFile: CloudFile): Recent {
        return with(cloudFile) {
            val account = context.accountOnline
            Recent(
                fileId = id,
                path = "",
                name = title,
                size = pureContentLength,
                ownerId = account?.id.orEmpty(),
                source = account?.portalUrl.orEmpty()
            )
        }
    }

    abstract fun getNextList()

    abstract fun onActionClick()

    abstract fun updateViewsState()

    companion object {

        val TAG: String = DocsBasePresenter::class.java.simpleName

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
        const val TAG_DIALOG_CANCEL_CONVERSION = "TAG_DIALOG_CANCEL_CONVERSION"
        const val TAG_DIALOG_CLEAR_DISPOSABLE = "TAG_DIALOG_CLEAR_DISPOSABLE"
        const val TAG_DIALOG_MOVE_TO_PUBLIC = "TAG_DIALOG_MOVE_TO_PUBLIC"

        /**
         * Requests values
         * */

        private const val ITEMS_PER_PAGE = 25
    }
}