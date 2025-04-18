package app.editors.manager.viewModels.main

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.providers.CloudFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.managers.works.DownloadWork
import app.editors.manager.mvp.models.ui.FileVersionUi
import app.editors.manager.mvp.models.ui.ResultUi
import app.editors.manager.mvp.models.ui.asFlowResultUI
import app.editors.manager.mvp.models.ui.toFileVersionUi
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import java.lang.ref.WeakReference

sealed interface VersionHistoryEvent : BaseEvent {
    data class RequestDocumentCreation(val fileName: String) : VersionHistoryEvent
    data class DownloadSuccessfully(val msg: String, val buttonText: String, val uri: Uri?): VersionHistoryEvent
}

sealed interface DialogState {
    data object Hidden : DialogState
    data class Loading(val item: ExplorerContextItem) : DialogState
    data class Idle(val item: ExplorerContextItem) : DialogState
}

data class VersionHistoryState(
    val historyResult: ResultUi<Map<String, List<FileVersionUi>>> = ResultUi.Loading,
    val currentItem: FileVersionUi? = null,
    val dialogState: DialogState = DialogState.Hidden,
    val isRefreshing: Boolean = false
)

fun interface VersionViewer {
    fun openFileVersion(file: CloudFile, onError: (Throwable) -> Unit)
}

class VersionHistoryViewModel(
    private val cloudFileProvider: CloudFileProvider,
    private val downloader: WorkManager,
    private val downloadReceiver: DownloadReceiver,
    private val resourceProvider: ResourcesProvider,
    private val fileId: String

) : BaseEventViewModel(resourceProvider), DownloadReceiver.BaseOnDownloadListener {

    private val _uiState = MutableStateFlow(VersionHistoryState())
    val uiState = _uiState.asStateFlow()

    private var viewerRef: WeakReference<VersionViewer>? = null

    init {
        downloadReceiver.addListener(this)
        viewModelScope.launch { getVersionHistory() }

    }

    fun onRefresh(delayMs: Long = 0, msgRes: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            msgRes?.let { sendMessage(it) }
            if (delayMs > 0) delay(delayMs)
            getVersionHistory()
        }
    }

    private suspend fun getVersionHistory() {
        cloudFileProvider.getVersionHistory(fileId)
            .asFlowResultUI(::mapResult).collect { result ->
                _uiState.update { it.copy(
                    historyResult = result,
                    isRefreshing = false
                ) }
            }
    }

    fun setSelectedItem(item: FileVersionUi){
        _uiState.update { it.copy(currentItem = item) }
    }

    fun onConfirmDialog(item: ExplorerContextItem){
        _uiState.update { it.copy(dialogState = DialogState.Loading(item)) }
        when(item){
            is ExplorerContextItem.Restore -> onRestoreClick()
            is ExplorerContextItem.DeleteVersion -> onDeleteClick()
            else -> {}
        }
    }

    fun onDismissDialog(){
        _uiState.update { it.copy(dialogState = DialogState.Hidden) }
    }

    fun handleContextMenuAction(item: ExplorerContextItem){
        if (_uiState.value.isRefreshing) return
        when(item){
            is ExplorerContextItem.Open -> onOpenClick()
            is ExplorerContextItem.Download -> onDownloadClick()
            is ExplorerContextItem.Restore, is ExplorerContextItem.DeleteVersion ->
                _uiState.update { it.copy(dialogState = DialogState.Idle(item)) }
            else -> {}
        }
    }

    private fun onDownloadClick(){
        viewModelScope.launch {
            _uiState.value.currentItem?.let { item ->
                _events.send(VersionHistoryEvent.RequestDocumentCreation(item.title))
            }
        }
    }

    private fun onOpenClick() {
        _uiState.value.currentItem?.let {
            viewerRef?.get()?.openFileVersion(
                CloudFile().apply {
                    id = it.fileId
                    version = it.version
                }
            ){
                sendMessage(R.string.error_version_open)
            } ?: sendMessage(R.string.error_version_open)
        }
    }

    private fun onRestoreClick() {
        handleVersionAction(
            successMsgId = R.string.version_restore_done,
            errorMsgId = R.string.error_version_restore
        ){ item ->
            cloudFileProvider.restoreVersion(item.fileId, item.version)
        }
    }

    private fun onDeleteClick() {
        handleVersionAction(
            successMsgId = R.string.version_delete_done,
            errorMsgId = R.string.error_version_delete
        ){ item ->
            cloudFileProvider.deleteVersion(item.fileId, item.version)
        }
    }

    private fun handleVersionAction(
        successMsgId: Int,
        errorMsgId: Int,
        apiCall: (FileVersionUi) -> Flow<Result<Unit>>
    ){
        viewModelScope.launch {
            uiState.value.currentItem?.let { item ->
                apiCall(item).collect { result ->
                    if (result.isSuccess) onRefresh(msgRes = successMsgId)
                    else sendMessage(errorMsgId)
                }
            }
            _uiState.update { it.copy(dialogState = DialogState.Hidden) }
        }
    }

    fun startDownloadWork(uri: Uri){
        _uiState.value.currentItem?.let { file ->
            val workData = Data.Builder()
                .putString(BaseDownloadWork.FILE_ID_KEY, "${file.fileId}${file.version}")
                .putString(BaseDownloadWork.URL_KEY, file.viewUrl)
                .putString(BaseDownloadWork.FILE_URI_KEY, uri.toString())
                .build()

            val request = OneTimeWorkRequest.Builder(DownloadWork::class.java)
                .setInputData(workData)
                .build()

            downloader.enqueue(request)
        }
    }

    override fun onDownloadError(info: String?) {
        sendMessage(R.string.download_manager_error)
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
        viewModelScope.launch {
            _events.send(VersionHistoryEvent.DownloadSuccessfully(
                msg = "$info\n$title",
                buttonText = resourceProvider.getString(R.string.download_manager_open),
                uri = uri
            ))
        }
    }

    private fun mapResult(files: List<CloudFile>): Map<String, MutableList<FileVersionUi>>{
        return files.map { it.toFileVersionUi() }
            .groupBy { it.versionGroup }
            .mapValues { entry ->
                entry.value.sortedByDescending { it.date }.toMutableList()
            }
    }

    override fun onCleared() {
        downloadReceiver.removeListener(this)
        super.onCleared()
    }

    fun setViewer(viewer: VersionViewer?){
        viewerRef = viewer?.let { WeakReference(it) }
    }

    companion object {
        fun Factory(fileId: String, viewer: VersionViewer?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as App)
                val downloader = WorkManager.getInstance(app.applicationContext)
                VersionHistoryViewModel(
                    cloudFileProvider = app.appComponent.cloudFileProvider,
                    downloader = downloader,
                    downloadReceiver = app.appComponent.downloadReceiver,
                    resourceProvider = app.appComponent.resourcesProvider,
                    fileId = fileId
                ).apply { setViewer(viewer) }
            }
        }
    }
}