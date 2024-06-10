package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
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
    val groups: List<Group> = emptyList(),
    val idAccessList: Map<String, Int> = emptyMap(),
)

sealed class InviteAccessEffect {

    data object Success : InviteAccessEffect()
    data class Error(val exception: Exception) : InviteAccessEffect()
}

open class InviteAccessViewModel(
    access: Int,
    users: List<User>,
    groups: List<Group>,
    emails: List<String> = emptyList()
) : ViewModel() {

    companion object {

        fun initState(
            access: Int,
            users: List<User>,
            groups: List<Group>,
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
                        groups.map(Group::id)
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

    open fun invite() {
        viewModelScope.launch {

        }
    }

    protected fun updateState(block: (InviteAccessState) -> InviteAccessState) {
        _state.update(block)
    }

    protected fun emitEffect(effect: InviteAccessEffect) {
        _effect.tryEmit(effect)
    }
}