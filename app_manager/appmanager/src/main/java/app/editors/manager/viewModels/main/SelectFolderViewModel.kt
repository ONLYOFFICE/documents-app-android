package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.manager.ManagerRepository
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.PathPart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SelectFolderState(
    val loading: Boolean = false,
    val items: List<Item> = emptyList(),
    val pathParts: List<PathPart> = emptyList(),
    val title: String = ""
)

class SelectFolderViewModel(private val managerRepository: ManagerRepository) : ViewModel() {

    private val _state: MutableStateFlow<SelectFolderState> = MutableStateFlow(SelectFolderState())
    val state: StateFlow<SelectFolderState> = _state.asStateFlow()

    fun openFolder(folderId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true) }
                _state.update {
                    with(managerRepository.getExplorer(folderId)) {
                        it.copy(
                            loading = false,
                            items = folders + files,
                            pathParts = pathParts,
                            title = current.title
                        )
                    }
                }
            } catch (_: Exception) {

            }
        }
    }

    fun backToPrevious(): Boolean {
        val state = _state.value
        return if (state.pathParts.size > 1) {
            openFolder(state.pathParts[state.pathParts.size - 2].id)
            true
        } else {
            false
        }
    }
}