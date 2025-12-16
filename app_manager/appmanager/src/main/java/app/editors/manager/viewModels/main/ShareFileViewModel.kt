package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.providers.CloudFileProvider
import app.editors.manager.app.App
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ShareFileViewModel(
    cloudFileProvider: CloudFileProvider,
    fileId: String
) : ViewModel() {

    val state: StateFlow<NetworkResult<CloudFile>> = cloudFileProvider.getFileInfo(fileId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = NetworkResult.Loading
    )

    companion object {
        fun Factory(fileId: String) : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                ShareFileViewModel(
                    cloudFileProvider = app.appComponent.cloudFileProvider,
                    fileId = fileId
                )
            }
        }
    }
}