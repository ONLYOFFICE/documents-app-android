package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.work.Data
import app.editors.manager.BuildConfig
import app.documents.core.network.manager.models.explorer.*
import app.documents.core.storage.recent.Recent
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.documents.core.providers.LocalFileProvider
import app.documents.core.providers.ProviderError
import app.documents.core.providers.WebDavFileProvider
import app.editors.manager.managers.works.UploadWork
import app.documents.core.network.manager.models.request.RequestCreate
import app.editors.manager.app.localFileProvider
import app.editors.manager.app.webDavFileProvider
import app.editors.manager.mvp.views.main.DocsOnDeviceView
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.*
import moxy.InjectViewState
import moxy.presenterScope
import java.io.File
import java.util.*

@InjectViewState
class DocsOnDevicePresenter : DocsBasePresenter<DocsOnDeviceView>() {

    companion object {
        val TAG: String = DocsOnDevicePresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
        fileProvider = context.localFileProvider
        checkWebDav()
    }

    private var webDavFileProvider: WebDavFileProvider? = null

    private fun checkWebDav() {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                if (it.isWebDav) {
                    webDavFileProvider = context.webDavFileProvider
                }
            }
        }
    }

    override fun getNextList() {
        // Stub to local
    }

    override fun createDocs(title: String) {
        val id = modelExplorerStack.currentId
        if (id != null) {
            val requestCreate = RequestCreate()
            requestCreate.title = title
            fileProvider?.let { provider ->
                disposable.add(provider.createFile(id, requestCreate)
                    .subscribe({ file: CloudFile ->
                        addFile(file)
                        addRecent(file)
                        openFile(file, true)
                    }) { viewState.onError(context.getString(R.string.errors_create_local_file)) })
            }
        }
    }

    override fun getFileInfo() {
        if (itemClicked != null && itemClicked is CloudFile) {
            val file = itemClicked as CloudFile
            addRecent(file)
            openFile(file)
        }
    }

    override fun addRecent(file: CloudFile) {
        presenterScope.launch {
            recentDao.addRecent(
                Recent(
                    idFile = null,
                    path = file.webUrl,
                    name = file.title,
                    size = file.pureContentLength,
                    isLocal = true,
                    isWebDav = false,
                    date = Date().time
                )
            )
        }

    }

    private fun addRecent(uri: Uri) {
        presenterScope.launch {
            DocumentFile.fromSingleUri(context, uri)?.let { file ->
                recentDao.addRecent(
                    Recent(
                        idFile = null,
                        path = uri.toString(),
                        name = file.name ?: "",
                        size = file.length(),
                        isLocal = true,
                        isWebDav = false,
                        date = Date().time,
                    )
                )
            }
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
            viewState.onStateActionButton(true)
            viewState.onActionBarTitle(
                currentTitle.takeIf(String::isNotEmpty)
                    ?: context.getString(R.string.toolbar_menu_search_result)
            )
        } else {
            if (isFoldersMode) {
                viewState.onActionBarTitle(context.getString(R.string.operation_title))
                viewState.onStateActionButton(false)
            } else {
                viewState.onActionBarTitle(context.getString(R.string.fragment_on_device_title))
                viewState.onStateActionButton(true)
            }
            viewState.onStateAdapterRoot(true)
            viewState.onStateUpdateRoot(true)
        }
    }

    override fun onActionClick() {
        viewState.onActionDialog()
    }

    override fun deleteItems() {
        val items = modelExplorerStack.selectedFiles + modelExplorerStack.selectedFolders
        fileProvider?.let { provider ->
            disposable.add(
                provider.delete(items, null)
                    .doOnComplete {
                        modelExplorerStack.removeSelected()
                        getBackStack()
                        setPlaceholderType(if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                        viewState.onRemoveItems(*items.toTypedArray())
                    }
                    .subscribe()
            )
        }
    }

    override fun uploadToMy(uri: Uri) {
        context.accountOnline?.let { account ->
            if (webDavFileProvider == null) {
                when {
                    preferenceTool.uploadWifiState && !NetworkUtils.isWifiEnable(context) -> {
                        viewState.onSnackBar(context.getString(R.string.upload_error_wifi))
                    }
                    ContentResolverUtils.getSize(context, uri) > FileUtils.STRICT_SIZE -> {
                        viewState.onSnackBar(context.getString(R.string.upload_manager_error_file_size))
                    }
                    else -> {
                        if (!account.isWebDav) {
                            val workData = Data.Builder()
                                .putString(UploadWork.TAG_UPLOAD_FILES, uri.toString())
                                .putString(UploadWork.ACTION_UPLOAD_MY, UploadWork.ACTION_UPLOAD_MY)
                                .putString(UploadWork.TAG_FOLDER_ID, null)
                                .build()
                            startUpload(workData)
                        }
                    }
                }
            } else {
                uploadWebDav(account.webDavPath ?: "", listOf(uri))
            }
        }
    }

    override fun openFolder(id: String?, position: Int) {
        setFiltering(false)
        super.openFolder(id, position)
    }

    override fun sendCopy() {
        itemClicked?.id?.let { path ->
            viewState.onSendCopy(File(path))
        }
    }

    private fun uploadWebDav(id: String, uriList: List<Uri>) {
        var uploadId = id
        if (uploadId[uploadId.length - 1] != '/') {
            uploadId = "$uploadId/"
        }
        uploadDisposable = webDavFileProvider?.upload(uploadId, uriList)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ }, { throwable: Throwable -> fetchError(throwable) }
            ) {
                viewState.onDialogClose()
                viewState.onSnackBar(context.getString(R.string.upload_manager_complete))
                for (file in (fileProvider as WebDavFileProvider).uploadsFile) {
                    addFile(file)
                }
                (fileProvider as WebDavFileProvider).uploadsFile.clear()
            }
    }

    override fun rename(title: String?) {
        val item = modelExplorerStack.getItemById(itemClicked)
        if (item != null) {
            val existFile = File(item.id)
            if (existFile.exists()) {
                val path = StringBuilder()
                path.append(existFile.parent).append("/").append(title)
                if (item is CloudFile) {
                    path.append(item.fileExst)
                }
                val renameFile = File(path.toString())
                if (renameFile.exists()) {
                    viewState.onError(context.getString(R.string.rename_file_exist))
                } else {
                    super.rename(title)
                }
            }
        }
    }

    fun moveFile(data: Uri?, isCopy: Boolean) {
        val path = PathUtils.getFolderPath(context, data!!)
        if (isSelectionMode) {
            moveSelection(path, isCopy)
            return
        }
        try {
            if ((fileProvider as LocalFileProvider).transfer(path, itemClicked, isCopy)) {
                refresh()
                viewState.onSnackBar(context.getString(R.string.operation_complete_message))
            } else {
                viewState.onError(context.getString(R.string.operation_error_move_to_same))
            }
        } catch (e: Exception) {
            catchTransferError(e)
        }
    }

    fun openFromChooser(uri: Uri) {
        val fileName = ContentResolverUtils.getName(context, uri)
        val ext = StringUtils.getExtensionFromPath(fileName.lowercase())

        addRecent(uri)
        openFile(uri, ext)
    }

    fun import(uri: Uri) {
        disposable.add((fileProvider as LocalFileProvider).import(
            context,
            modelExplorerStack.currentId ?: throw RuntimeException(),
            uri
        )
            .subscribe(
                {},
                { throwable ->
                    deleteImportFailedFile(uri)
                    viewState.onError(throwable.message)
                }
            ) {
                refresh()
                viewState.onSnackBar(context.getString(R.string.operation_complete_message))
            })
    }

    @SuppressLint("MissingPermission")
    private fun deleteImportFailedFile(uri: Uri?) {
        uri?.let {
            val parentFile = File(modelExplorerStack.currentId ?: return)
            val path = PathUtils.getPath(context, uri)
            Uri.parse(path).path?.let { filePath -> File(filePath) }?.let { file ->
                FileUtils.deletePath(File(parentFile, file.name))
            }
        }

    }

    private fun openFile(file: CloudFile, viewMode: Boolean = true) {
        val path = file.id
        val uri = Uri.fromFile(File(path))
        val ext = StringUtils.getExtensionFromPath(file.id.lowercase())
        openFile(uri, ext, viewMode)
    }

    @Suppress("KotlinConstantConditions")
    private fun openFile(uri: Uri, ext: String, viewMode: Boolean = true) {
        when (val enumExt = StringUtils.getExtension(ext)) {
            StringUtils.Extension.DOC, StringUtils.Extension.HTML, StringUtils.Extension.EBOOK, StringUtils.Extension.FORM -> {
                if (BuildConfig.APPLICATION_ID != "com.onlyoffice.documents" && enumExt == StringUtils.Extension.FORM) {
                    viewState.onError(context.getString(R.string.error_unsupported_format))
                } else {
                    viewState.onShowDocs(uri, viewMode)
                }
            }
            StringUtils.Extension.SHEET -> viewState.onShowCells(uri)
            StringUtils.Extension.PRESENTATION -> viewState.onShowSlides(uri)
            StringUtils.Extension.PDF -> viewState.onShowPdf(uri)
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> showMedia(uri)
            else -> viewState.onError(context.getString(R.string.error_unsupported_format))
        }
    }

    private fun moveSelection(path: String?, isCopy: Boolean) {
        if (modelExplorerStack.countSelectedItems > 0) {
            if (fileProvider is LocalFileProvider) {
                val provider = fileProvider as LocalFileProvider
                val items: MutableList<Item> = ArrayList()
                val files = modelExplorerStack.selectedFiles
                val folders = modelExplorerStack.selectedFolders
                items.addAll(folders)
                items.addAll(files)
                for (item in items) {
                    try {
                        if (!provider.transfer(path, item, isCopy)) {
                            viewState.onError(context.getString(R.string.operation_error_move_to_same))
                            break
                        }
                    } catch (e: Exception) {
                        catchTransferError(e)
                    }
                }
                getBackStack()
                refresh()
                viewState.onSnackBar(context.getString(R.string.operation_complete_message))
            }
        } else {
            viewState.onError(context.getString(R.string.operation_empty_lists_data))
        }
    }

    fun deleteFile() {
        itemClicked?.let { item ->
            fileProvider?.let { provider ->
                disposable.add(
                    provider.delete(listOf(item), null)
                        .doOnComplete {
                            modelExplorerStack.removeItemById(item.id)
                            viewState.onRemoveItems(item)
                        }
                        .subscribe()
                )
            }
        }
    }

    fun upload() {
        itemClicked?.let { item ->
            context.accountOnline?.let {
                Uri.fromFile(File(item.id))?.let { uri ->
                    uploadToMy(uri)
                }
            } ?: run {
                viewState.onShowPortals()
            }
        }

    }

    @SuppressLint("MissingPermission")
    fun showMedia(uri: Uri) {
        viewState.onOpenMedia(OpenState.Media(getMediaFile(uri), false))
    }

    private fun getMediaFile(uri: Uri): Explorer {
        return Explorer().apply {
            val explorerFile = CloudFile()
            if (uri.scheme == "content") {
                val file = DocumentFile.fromSingleUri(context, uri)
                explorerFile.apply {
                    pureContentLength = file?.length() ?: 0
                    webUrl = uri.toString()
                    fileExst = StringUtils.getExtensionFromPath(file?.name ?: "")
                    title = file?.name ?: ""
                    isClicked = true
                }
                current = Current().apply {
                    title = file?.name ?: ""
                    filesCount = "1"
                }
                files = mutableListOf(explorerFile)
            } else {
                val file = File(PathUtils.getPath(context, uri).toString())
                explorerFile.apply {
                    pureContentLength = file.length()
                    webUrl = file.absolutePath
                    fileExst = StringUtils.getExtensionFromPath(file.name)
                    title = file.name
                    isClicked = true
                }
                current = Current().apply {
                    title = file.name
                    filesCount = "1"
                }
                files = mutableListOf(explorerFile)
            }

            addRecent(explorerFile)
        }
    }

    override fun fetchError(throwable: Throwable) {
        if (throwable.message != null) {
            if (throwable.message == ProviderError.ERROR_CREATE_LOCAL) {
                viewState.onError(context.getString(R.string.rename_file_exist))
            } else {
                super.fetchError(throwable)
            }
        }
    }

    private fun catchTransferError(e: Exception) {
        if (e.message != null) {
            when (e.message) {
                ProviderError.FILE_EXIST -> viewState.onError(context.getString(R.string.operation_error_move_to_same))
                ProviderError.UNSUPPORTED_PATH -> viewState.onError(context.getString(R.string.error_unsupported_path))
            }
        } else {
            Log.e(TAG, "Error move/copy local")
        }
    }

    fun updateState() {
        setSelection(false)
        setFiltering(false)
        updateViewsState()
    }

    fun getFileInfo(viewMode: Boolean) {
        if (itemClicked != null && itemClicked is CloudFile) {
            val file = itemClicked as CloudFile
            addRecent(file)
            openFile(file, viewMode)
        }
    }
}