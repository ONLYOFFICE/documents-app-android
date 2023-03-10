package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.net.Uri
import app.documents.core.account.Recent
import app.documents.core.network.ApiContract
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.webDavApi
import app.editors.manager.managers.providers.WebDavFileProvider
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.views.main.DocsWebDavView
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.receivers.ExportReceiver
import lib.toolkit.base.managers.receivers.ExportReceiver.Companion.getFilters
import lib.toolkit.base.managers.receivers.ExportReceiver.OnExportFile
import lib.toolkit.base.managers.utils.ContentResolverUtils.getSize
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.FileUtils.asyncDeletePath
import lib.toolkit.base.managers.utils.NetworkUtils.isWifiEnable
import lib.toolkit.base.managers.utils.PermissionUtils.checkReadWritePermission
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils.formatDate
import moxy.InjectViewState
import java.util.*

@InjectViewState
class DocsWebDavPresenter : DocsBasePresenter<DocsWebDavView>() {

    companion object {
        val TAG: String = DocsWebDavPresenter::class.java.simpleName
    }


    private val exportReceiver: ExportReceiver = ExportReceiver()
    private var tempFile: CloudFile? = null

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getProvider() {
        fileProvider?.let {
            context.accountOnline?.let {
                getItemsById(it.webDavPath)
            }
        } ?: run {
            context.accountOnline?.let { cloudAccount ->
                fileProvider = WebDavFileProvider(
                    context.webDavApi(),
                    WebDavApi.Providers.valueOf(cloudAccount.webDavProvider ?: "")
                )
                getItemsById(cloudAccount.webDavPath)
            }
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        exportReceiver.onExportReceiver = object : OnExportFile {
            override fun onExportFile(uri: Uri) {
                upload(uri, null)
            }
        }
        context.registerReceiver(exportReceiver, getFilters())
    }

    override fun onDestroy() {
        super.onDestroy()
        exportReceiver.onExportReceiver = null
        context.unregisterReceiver(exportReceiver)
    }

    override fun getNextList() {
        // Stub
    }

    override fun createDocs(title: String) {
        val id = modelExplorerStack.currentId
        if (id != null) {
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

    override fun getFileInfo() {
        if (itemClicked != null && itemClicked is CloudFile) {
            val file = itemClicked as CloudFile
            val extension = file.fileExst
            if (StringUtils.isImage(extension)) {
                addRecent(file)
                viewState.onFileMedia(removeVideo(getListMedia(file.id)), true)
                return
            }
        }
        showDialogWaiting(TAG_DIALOG_CANCEL_UPLOAD)
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
            viewState.onActionBarTitle(currentTitle)
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
        state.info = formatDate(itemClickedDate)
        state.isFolder = item is CloudFolder
        state.isWebDav = true
        state.isOneDrive = false
        state.isGoogleDrive = false
        state.isDropBox = false
        state.isTrash = isTrash
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

    override fun upload(uri: Uri?, uris: List<Uri>?, tag: String?) {
        if (preferenceTool.uploadWifiState && !isWifiEnable(context)) {
            viewState.onSnackBar(context.getString(R.string.upload_error_wifi))
            return
        }
        val id = modelExplorerStack.currentId
        if (id != null) {
            val uploadUris: MutableList<Uri> = ArrayList()
            if (uri != null) {
                uploadUris.add(uri)
            }
            if (uris != null && uris.isNotEmpty()) {
                for (i in uris.indices) {
                    uploadUris.add(uris[i])
                }
            }
            uploadWebDav(id, uploadUris)
        }
    }

    override fun uploadToMy(uri: Uri) {
        if (preferenceTool.uploadWifiState && !isWifiEnable(context)) {
            viewState.onSnackBar(context.getString(R.string.upload_error_wifi))
            return
        }
        if (getSize(context, uri) > FileUtils.STRICT_SIZE) {
            viewState.onSnackBar(context.getString(R.string.upload_manager_error_file_size))
            return
        }
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                if (it.isWebDav) {
                    withContext(Dispatchers.Main) {
                        uploadWebDav(it.webDavPath ?: "/", listOf(uri))
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun uploadWebDav(id: String, uriList: List<Uri>) {
        var uploadId = id
        if (uploadId[uploadId.length - 1] != '/') {
            uploadId = "$uploadId/"
        }
        showDialogWaiting(TAG_DIALOG_CANCEL_UPLOAD)
        uploadDisposable = fileProvider?.upload(uploadId, uriList)!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ }, { throwable: Throwable ->
                fetchError(throwable)
                if (tempFile != null && tempFile!!.webUrl != "") {
                    asyncDeletePath(Uri.parse(tempFile!!.webUrl).path!!)
                }
            }
            ) {
                refresh()
                deleteTempFile()
                viewState.onDialogClose()
                viewState.onSnackBar(context.getString(R.string.upload_manager_complete))
                (fileProvider as WebDavFileProvider).uploadsFile.clear()
            }
    }

    override fun terminate() {
        super.terminate()
        if (downloadDisposable != null) {
            downloadDisposable!!.dispose()
        }
    }

    @SuppressLint("MissingPermission")
    fun deleteTempFile() {
        if (tempFile != null && checkReadWritePermission(context)) {
            val uri = Uri.parse(tempFile?.webUrl)
            if (uri.path != null) {
                asyncDeletePath(uri.path ?: "")
            }
        }
    }

    private fun removeVideo(listMedia: Explorer): Explorer {
        val files: MutableList<CloudFile> = ArrayList()
        for (file in listMedia.files) {
            if (StringUtils.isImage(file.fileExst)) {
                files.add(file)
            }
        }
        listMedia.files = files
        return listMedia
    }

}