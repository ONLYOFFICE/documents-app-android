package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.mapResult
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.providers.RoomProvider
import app.editors.manager.app.App
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.tools.PreferenceTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TemplateListState(
    val templates: List<CloudFolder> = emptyList(),
    val sortBy: String? = null
)

class RoomFromTemplateViewModel(
    private val roomProvider: RoomProvider,
    private val preferenceTool: PreferenceTool
) : ViewModel() {

    private val _uiState: MutableStateFlow<NetworkResult<TemplateListState>> =
        MutableStateFlow(NetworkResult.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        val filters = mutableMapOf<String, String>().apply {
            put(ApiContract.Parameters.ARG_SORT_BY, preferenceTool.sortBy ?: "")
            put(ApiContract.Parameters.ARG_SORT_ORDER, preferenceTool.sortOrder ?: "")
        }
        viewModelScope.launch {
            roomProvider.getRoomTemplates(filters).mapResult { list ->
                TemplateListState(
                    templates = list,
                    sortBy = preferenceTool.sortBy
                )
            }.collect { result -> _uiState.update { result } }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as App)
                RoomFromTemplateViewModel(
                    roomProvider = app.roomProvider,
                    preferenceTool = app.appComponent.preference
                )
            }
        }
    }
}