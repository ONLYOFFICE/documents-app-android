package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.work.Data
import app.documents.core.account.Recent
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.webDavApi
import app.editors.manager.managers.providers.LocalFileProvider
import app.editors.manager.managers.providers.ProviderError
import app.editors.manager.managers.providers.WebDavFileProvider
import app.editors.manager.managers.works.UploadWork
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.views.main.DocsOnDeviceView
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.*
import moxy.InjectViewState
import java.io.File
import java.util.*

@InjectViewState
class DocsOnDevicePresenter : DocsBasePresenter<DocsOnDeviceView>() {

    companion object {
        val TAG: String = DocsOnDevicePresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
        fileProvider = LocalFileProvider(LocalContentTools(context))
        checkWebDav()
    }

    private var photoUri: Uri? = null
    private var webDavFileProvider: WebDavFileProvider? = null

    private fun checkWebDav() {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                if (it.isWebDav) {
                    webDavFileProvider = WebDavFileProvider(
                        context.webDavApi(),
                        WebDavApi.Providers.valueOf(it.webDavProvider ?: "")
                    )
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
                        openFile(file)
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
        CoroutineScope(Dispatchers.Default).launch {
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
        CoroutineScope(Dispatchers.Default).launch {
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
            viewState.onActionBarTitle(currentTitle.takeIf(String::isNotEmpty)
                ?: context.getString(R.string.toolbar_menu_search_result))
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

    override fun onContextClick(item: Item, position: Int, isTrash: Boolean) {
        onClickEvent(item, position)
        isContextClick = true
        val state = ContextBottomDialog.State()
        val onlineAccount = context.accountOnline
        state.isDropBox = onlineAccount?.isDropbox == true
        state.isOneDrive = onlineAccount?.isOneDrive == true
        state.isGoogleDrive = onlineAccount?.isGoogleDrive == true
        state.isVisitor = onlineAccount?.isVisitor == true
        state.isLocal = true
        state.title = item.title
        state.info = TimeUtils.formatDate(itemClickedDate)
        state.isFolder = item is CloudFolder
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
        viewState.onActionDialog()
    }

    override fun deleteItems() {
        val items: MutableList<Item> = ArrayList()
        val files = modelExplorerStack.selectedFiles
        val folders = modelExplorerStack.selectedFolders
        items.addAll(folders)
        items.addAll(files)
        fileProvider?.let { provider ->
            disposable.add(provider.delete(items, null)
                .subscribe({ }, { fetchError(it) }) {
                    modelExplorerStack.removeSelected()
                    getBackStack()
                    setPlaceholderType(if (modelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                    viewState.onRemoveItems(items)
                    viewState.onSnackBar(context.getString(R.string.operation_complete_message))
                })
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

    private fun openFile(file: CloudFile) {
        val path = file.id
        val uri = Uri.fromFile(File(path))
        val ext = StringUtils.getExtensionFromPath(file.id.lowercase())
        openFile(uri, ext)
    }

    private fun openFile(uri: Uri, ext: String) {
        when (StringUtils.getExtension(ext)) {
            StringUtils.Extension.DOC, StringUtils.Extension.HTML, StringUtils.Extension.EBOOK, StringUtils.Extension.FORM -> viewState.onShowDocs(uri)
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

    fun showDeleteDialog() {
        if (itemClicked != null) {
            if (itemClicked is CloudFolder) {
                viewState.onDialogQuestion(
                    context.getString(R.string.dialogs_question_delete),
                    context.getString(R.string.dialog_question_delete_folder),
                    TAG_DIALOG_DELETE_CONTEXT
                )
            } else {
                viewState.onDialogQuestion(
                    context.getString(R.string.dialogs_question_delete),
                    context.getString(R.string.dialog_question_delete_file),
                    TAG_DIALOG_DELETE_CONTEXT
                )
            }
        }
    }

    fun deleteFile() {
        if (itemClicked != null) {
            val items: MutableList<Item> = ArrayList()
            items.add(itemClicked!!)
            fileProvider?.let { provider ->
                disposable.add(provider.delete(items, null)
                    .subscribe({ }, { }) {
                        modelExplorerStack.removeItemById(itemClicked?.id)
                        viewState.onRemoveItem(itemClicked)
                        viewState.onSnackBar(context.getString(R.string.operation_complete_message))
                    })
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun createPhoto() {
        val photo = FileUtils.createFile(File(stack?.current?.id ?: ""), TimeUtils.fileTimeStamp, "png")
        if (photo != null) {
            photoUri = ContentResolverUtils.getFileUri(context, photo)
            viewState.onShowCamera(photoUri)
        }
    }

    fun deletePhoto() {
        if (photoUri != null) {
            context.contentResolver.delete(photoUri!!, null, null)
        }
    }

    fun checkSelectedFiles() {
        if (modelExplorerStack.countSelectedItems > 0) {
            viewState.onShowFolderChooser()
        } else {
            viewState.onError(context.getString(R.string.operation_empty_lists_data))
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

    private fun getMediaFile(uri: Uri): Explorer =
        Explorer().apply {
            val file = File(PathUtils.getPath(context, uri).toString())
            val explorerFile = CloudFile().apply {
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
            files = listOf(explorerFile)
            addRecent(explorerFile)
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
}