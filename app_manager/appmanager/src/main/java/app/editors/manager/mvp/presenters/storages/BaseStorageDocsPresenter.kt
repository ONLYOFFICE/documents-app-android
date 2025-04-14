package app.editors.manager.mvp.presenters.storages

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.WorkManager
import app.documents.core.model.cloud.Recent
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.providers.BaseFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.mvp.views.base.BaseStorageDocsView
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.StringUtils
import moxy.presenterScope

abstract class BaseStorageDocsPresenter<V : BaseStorageDocsView, FP : BaseFileProvider> : DocsBasePresenter<V, FP>(),
    UploadReceiver.OnUploadListener, DownloadReceiver.OnDownloadListener {

    abstract val externalLink: Unit

    var tempFile: CloudFile? = null
    val workManager = WorkManager.getInstance(App.getApp())

    private val uploadReceiver: UploadReceiver = UploadReceiver()
    private val downloadReceiver: DownloadReceiver = DownloadReceiver()

    companion object {
        const val DOWNLOAD_ZIP_NAME =  "storage.zip"
    }

    abstract fun startDownload(downloadTo: Uri, item: Item?)

    abstract fun refreshToken()

    abstract fun getItems()

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

    override fun createDownloadFile() {
        if (modelExplorerStack.countSelectedItems == 0) {
            if (itemClicked is CloudFolder) {
                viewState.onCreateDownloadFile(DOWNLOAD_ZIP_NAME)
            } else if (itemClicked is CloudFile) {
                viewState.onCreateDownloadFile((itemClicked as CloudFile).title)
            }
        } else {
            viewState.onChooseDownloadFolder()
        }
    }

    override fun download(downloadTo: Uri) {
        if (modelExplorerStack.countSelectedItems == 0) {
            startDownload(downloadTo, itemClicked)
        } else {
            val itemList =  modelExplorerStack.selectedFiles + modelExplorerStack.selectedFolders
            itemList.forEach { item ->
                val fileName = if (item is CloudFile) item.title else DOWNLOAD_ZIP_NAME
                val doc = DocumentFile.fromTreeUri(context, downloadTo)?.createFile(
                    StringUtils.getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf("."))), fileName
                )
                doc?.uri?.let { uri -> startDownload(uri, item) }
            }
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
                        source = it.portal.url
                    )
                )
            }
        }
    }

    override fun delete(): Boolean {
        if (modelExplorerStack.countSelectedItems > 0) {
            viewState.onDialogDelete(
                modelExplorerStack.countSelectedItems,
                false,
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
            viewState.onActionBarTitle(currentTitle.ifEmpty { itemClicked?.title })
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

    override fun onDownloadError(info: String?) {
        viewState.onDialogClose()
        viewState.onSnackBar(info ?: context.getString(R.string.download_manager_error))
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
        if (isSelectionMode) {
            setSelection(false)
            updateViewsState()
        }
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

    override fun onUploadErrorDialog(title: String, message: String, file: String?) {}

    override fun onUploadComplete(
        path: String?,
        info: String?,
        title: String?,
        file: CloudFile?,
        id: String?
    ) {
        info?.let { viewState.onSnackBar(it) }
        refresh()
    }

    override fun onActionClick() {
        viewState.onActionDialog(false, true, null)
    }

    override fun onUploadAndOpen(path: String?, title: String?, file: CloudFile?, id: String?) {
        // Nothing
    }

    override fun onUploadFileProgress(progress: Int, id: String?, folderId: String?) {
        // Nothing
    }

    override fun onUploadCanceled(path: String?, info: String?, id: String?) {
        info?.let { viewState.onSnackBar(it) }
    }

    override fun onUploadRepeat(path: String?, info: String?) {
        viewState.onDialogClose()
        info?.let { viewState.onSnackBar(it) }
    }

    private fun showDownloadFolderActivity(uri: Uri?) {
        viewState.onDownloadActivity(uri)
    }

}