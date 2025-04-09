package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.Recent
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestDownload
import app.documents.core.providers.WebDavFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.webDavFileProvider
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.managers.works.WebDavDownloadWork
import app.editors.manager.mvp.views.main.DocsWebDavView
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.receivers.ExportReceiver
import lib.toolkit.base.managers.receivers.ExportReceiver.OnExportFile
import lib.toolkit.base.managers.utils.ContentResolverUtils.getSize
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.FileUtils.asyncDeletePath
import lib.toolkit.base.managers.utils.NetworkUtils.isWifiEnable
import lib.toolkit.base.managers.utils.PermissionUtils.checkReadWritePermission
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.presenterScope


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
        val path = (context.accountOnline?.portal?.provider as? PortalProvider.Webdav)?.path
        fileProvider?.let {
            getItemsById(path)
        } ?: run {
            fileProvider = context.webDavFileProvider
            getItemsById(path)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        exportReceiver.onExportReceiver = object : OnExportFile {
            override fun onExportFile(uri: Uri) {
                upload(uri, null)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(exportReceiver, ExportReceiver.getFilters(), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(exportReceiver, ExportReceiver.getFilters())
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        exportReceiver.onExportReceiver = null
        try {
            context.unregisterReceiver(exportReceiver)
        } catch (error: IllegalArgumentException) {
            error.printStackTrace()
        }
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
                disposable.add(
                    provider.createFile(id, requestCreate)
                        .subscribe({ file: CloudFile? ->
                            addFile(file)
                            setPlaceholderType(PlaceholderViews.Type.NONE)
                            viewState.onDialogClose()
                            viewState.onOpenLocalFile(file, null)
                        }, ::fetchError)
                )
            }
            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        }
    }

    override fun addRecent(file: CloudFile) {
        presenterScope.launch {
            context.accountOnline?.let {
                recentDataSource.addRecent(
                    Recent(
                        fileId = if (StringUtils.isImage(file.fileExst)) file.id else file.viewUrl,
                        path = file.webUrl,
                        name = file.title,
                        size = file.pureContentLength,
                        isWebdav = true,
                        ownerId = it.id,
                        source = it.portalUrl
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
            if (pickerMode == PickerMode.Folders) {
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
            context.accountOnline?.let {
                val webDavProvider = it.portal.provider
                if (webDavProvider is PortalProvider.Webdav) {
                    withContext(Dispatchers.Main) {
                        uploadWebDav(webDavProvider.path, listOf(uri))
                    }
                }
            }
        }
    }

    override fun startDownloadWork(
        to: Uri,
        id: String?,
        url: String?,
        requestDownload: RequestDownload?,
        worker: Class<out BaseDownloadWork>
    ) {
        super.startDownloadWork(to, id, url, requestDownload, WebDavDownloadWork::class.java)
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