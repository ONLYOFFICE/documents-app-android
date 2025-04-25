package app.editors.manager.viewModels.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import app.documents.core.providers.CloudFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.ui.fragments.main.versionhistory.VersionHistoryFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider

sealed interface EditCommentEvent : BaseEvent {
    data object UpdateHistory : EditCommentEvent
}

data class EditCommentState(
    val fileId: String,
    val version: Int,
    val comment: String,
    val isLoading: Boolean
)

class VersionEditCommentViewModel(
    private val cloudFileProvider: CloudFileProvider,
    private val resourceProvider: ResourcesProvider,
    savedStateHandle: SavedStateHandle
) : BaseEventViewModel(resourceProvider) {

    private val data = savedStateHandle.toRoute<VersionHistoryFragment.Screens.EditComment>()
    private val _uiState = MutableStateFlow(
        EditCommentState(
            fileId = data.fileId,
            version = data.version,
            comment = data.comment,
            isLoading = false
        )
    )
    val uiState = _uiState.asStateFlow()

    fun saveComment() {
        viewModelScope.launch {
            with(_uiState.value) {
                cloudFileProvider.editVersionComment(fileId, version, comment)
                    .onStart { _uiState.update { it.copy(isLoading = true) } }
                    .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                    .collect { result ->
                        if (result.isSuccess) _events.send(EditCommentEvent.UpdateHistory)
                        else sendMessage(R.string.error_edit_comment)
                    }
            }
        }
    }

    fun onCommentChange(newComment: String){
        _uiState.update { it.copy(comment = newComment) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appComponent = (this[APPLICATION_KEY] as App).appComponent
                VersionEditCommentViewModel(
                    cloudFileProvider = appComponent.cloudFileProvider,
                    resourceProvider = appComponent.resourcesProvider,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}