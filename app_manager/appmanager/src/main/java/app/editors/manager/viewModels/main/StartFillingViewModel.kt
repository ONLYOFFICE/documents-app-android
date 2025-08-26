package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.User
import app.documents.core.network.common.NetworkResult
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.managers.tools.BaseEvent
import app.editors.manager.managers.tools.BaseEventSender
import app.editors.manager.managers.tools.EventSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.FormRole

sealed interface StartFillingEvent : BaseEvent {
    data object Success : StartFillingEvent
}

data class StartFillingState(
    val rolesWithUsers: List<Pair<FormRole, User?>>,
    val isLoading: Boolean = false
)

class StartFillingViewModel(
    private val roomProvider: RoomProvider,
    private val roomId: String,
    private val formId: String,
    formRoles: List<Pair<FormRole, User?>>,
    resourcesProvider: ResourcesProvider
) : ViewModel(),
    EventSender by BaseEventSender(resourcesProvider) {

    private val _state: MutableStateFlow<StartFillingState> =
        MutableStateFlow(StartFillingState(formRoles))
    val state: StateFlow<StartFillingState> = _state.asStateFlow()

    fun setUser(index: Int, user: User) {
        updateRole(index, user)
    }

    fun deleteUser(index: Int) {
        updateRole(index, null)
    }

    fun startFilling() {
        viewModelScope.launch {
            roomProvider.startFilling(roomId, formId, state.value.rolesWithUsers)
                .onStart { _state.update { it.copy(isLoading = true) } }
                .onCompletion { _state.update { it.copy(isLoading = false) } }
                .collect { result ->
                    when (result) {
                        is NetworkResult.Error -> sendMessage(R.string.errors_unknown_error)
                        is NetworkResult.Success<*> -> sendEvent(StartFillingEvent.Success)
                        else -> Unit
                    }
                }
        }
    }

    private fun updateRole(index: Int, user: User?) {
        _state.update { state ->
            state.copy(
                rolesWithUsers = state.rolesWithUsers.toMutableList()
                    .apply { this[index] = this[index].first to user }
            )
        }
    }
}