package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.providers.RoomProvider
import app.editors.manager.managers.utils.RoomUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InviteByEmailState(
    val loading: Boolean = false,
    val emails: Map<String, Int> = emptyMap()
)

sealed class InviteByEmailEffect {
    data class Error(val exception: Exception) : InviteByEmailEffect()
    data object Success : InviteByEmailEffect()
    data object None : InviteByEmailEffect()
}

class InviteByEmailViewModel(
    private val roomId: String,
    private val roomType: Int,
    private val roomProvider: RoomProvider
) : ViewModel() {

    private val _state: MutableStateFlow<InviteByEmailState> = MutableStateFlow(InviteByEmailState())
    val state: StateFlow<InviteByEmailState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<InviteByEmailEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<InviteByEmailEffect> = _effect.asSharedFlow()

    fun add(email: String) {
        _state.update {
            it.copy(
                emails = _state.value.emails
                    .plus(email to (RoomUtils.getAccessOptions(roomType, false).lastOrNull() ?: 2))
            )
        }
    }

    fun remove(email: String) {
        _state.update {
            it.copy(emails = _state.value.emails.minus(email))
        }
    }

    fun setAccess(email: String, access: Int) {
        if (access == ApiContract.ShareCode.NONE) {
            remove(email)
        } else {
            _state.update {
                it.copy(emails = HashMap(_state.value.emails).apply { this[email] = access })
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true) }
                roomProvider.inviteByEmail(roomId, state.value.emails)
                _state.update { it.copy(emails = emptyMap()) }
                _effect.emit(InviteByEmailEffect.Success)
                _effect.emit(InviteByEmailEffect.None)
            } catch (e: Exception) {
                _effect.emit(InviteByEmailEffect.Error(e))
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }
}