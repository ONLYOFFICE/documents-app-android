package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.network.share.ShareService
import app.documents.core.network.share.models.request.RequestShare
import app.documents.core.network.share.models.request.RequestShareItem
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
    val access: Access,
    val emails: List<String> = emptyList(),
    val users: List<User> = emptyList(),
    val groups: List<Group> = emptyList(),
    val idAccessList: Map<String, Access> = emptyMap(),
)

sealed class InviteAccessEffect {

    data object Success : InviteAccessEffect()
    data class Error(val exception: Exception) : InviteAccessEffect()
}

open class InviteAccessViewModel(
    access: Access,
    users: List<User>,
    groups: List<Group>,
    emails: List<String> = emptyList(),
    private val shareService: ShareService? = null,
    private val itemId: String? = null,
    private val isFolder: Boolean = false
) : ViewModel() {

    companion object {

        fun initState(
            access: Access,
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
                                    it != Access.RoomManager
                                } ?: Access.ContentCreator
                            }
            )
        }
    }

    private val _state: MutableStateFlow<InviteAccessState> = MutableStateFlow(initState(access, users, groups, emails))
    val state: StateFlow<InviteAccessState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<InviteAccessEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<InviteAccessEffect> = _effect.asSharedFlow()

    fun setAccess(emailOrId: String, access: Access) {
        if (access == Access.None) {
            _state.update { it.copy(idAccessList = it.idAccessList.minus(emailOrId)) }
        } else {
            _state.update { it.copy(idAccessList = it.idAccessList.toMutableMap().apply { this[emailOrId] = access }) }
        }
    }

    fun setAllAccess(access: Access) {
        _state.update {
            it.copy(
                access = access,
                idAccessList = it.idAccessList.toMutableMap().mapValues { access }
            )
        }
    }

    open fun invite() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true) }
                val api = checkNotNull(shareService) { "api can't be null" }
                val itemId = checkNotNull(itemId) { "item id can't be null" }

                val request = RequestShare(
                    share = state.value.users.map(User::id)
                        .plus(state.value.groups.map(Group::id))
                        .associateWith { state.value.idAccessList[it] }
                        .mapNotNull { (id, access) -> access?.let { RequestShareItem(id, it.toString()) } }
                )
                if (isFolder) {
                    api.setFolderAccess(itemId, request)
                } else {
                    api.setFileAccess(itemId, request)
                }
                emitEffect(InviteAccessEffect.Success)
            } catch (e: Exception) {
                emitEffect(InviteAccessEffect.Error(e))
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    protected fun updateState(block: (InviteAccessState) -> InviteAccessState) {
        _state.update(block)
    }

    protected fun emitEffect(effect: InviteAccessEffect) {
        _effect.tryEmit(effect)
    }
}