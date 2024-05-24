package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.RoomGroup
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InviteAccessState(
    val loading: Boolean = false,
    val access: Int,
    val emails: List<String> = emptyList(),
    val users: List<User> = emptyList(),
    val groups: List<RoomGroup> = emptyList(),
    val idAccessList: Map<String, Int> = emptyMap(),
)

sealed class InviteAccessEffect {

    data object Success : InviteAccessEffect()
    data class Error(val exception: Exception) : InviteAccessEffect()
}

class InviteAccessViewModel(
    private val roomId: String,
    private val roomProvider: RoomProvider,
    access: Int,
    users: List<User>,
    groups: List<RoomGroup>,
    emails: List<String>
) : ViewModel() {

    companion object {

        fun initState(
            access: Int,
            users: List<User>,
            groups: List<RoomGroup>,
            emails: List<String>
        ): InviteAccessState {
            return InviteAccessState(
                access = access,
                emails = emails,
                users = users,
                groups = groups,
                idAccessList = users.map(User::id)
                    .plus(emails)
                    .associateWith { access } +
                        groups.map(RoomGroup::id)
                            .associateWith {
                                access.takeIf {
                                    it !in arrayOf(
                                        ApiContract.ShareCode.ROOM_ADMIN,
                                        ApiContract.ShareCode.POWER_USER
                                    )
                                } ?: ApiContract.ShareCode.READ
                            }
            )
        }
    }

    private val _state: MutableStateFlow<InviteAccessState> = MutableStateFlow(initState(access, users, groups, emails))
    val state: StateFlow<InviteAccessState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<InviteAccessEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<InviteAccessEffect> = _effect.asSharedFlow()

    fun setAccess(emailOrId: String, access: Int) {
        if (access == ApiContract.ShareCode.NONE) {
            _state.update { it.copy(idAccessList = it.idAccessList.minus(emailOrId)) }
        } else {
            _state.update { it.copy(idAccessList = it.idAccessList.toMutableMap().apply { this[emailOrId] = access }) }
        }
    }

    fun setAllAccess(access: Int) {
        _state.update {
            it.copy(
                access = access,
                idAccessList = it.idAccessList.toMutableMap().mapValues { access }
            )
        }
    }

    fun invite() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true) }
                if (_state.value.emails.isNotEmpty()) {
                    roomProvider.inviteByEmail(
                        roomId,
                        state.value.emails.associateWith { _state.value.idAccessList[it] ?: 2 }
                    )
                } else {
                    roomProvider.inviteById(
                        roomId,
                        state.value.users
                            .map(User::id)
                            .plus(state.value.groups.map(RoomGroup::id))
                            .associateWith { _state.value.idAccessList[it] ?: 2 }
                    )
                }
                _effect.emit(InviteAccessEffect.Success)
            } catch (e: Exception) {
                _effect.emit(InviteAccessEffect.Error(e))
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }
}