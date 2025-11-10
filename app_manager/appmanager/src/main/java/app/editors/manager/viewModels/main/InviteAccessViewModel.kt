package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Email
import app.documents.core.model.login.Group
import app.documents.core.model.login.Member
import app.documents.core.model.login.User
import app.documents.core.network.share.ShareService
import app.documents.core.network.share.models.request.RequestShare
import app.documents.core.network.share.models.request.RequestShareItem
import app.editors.manager.managers.tools.ShareData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
data class InviteAccessState(
    val loading: Boolean = false,
    val commonAccess: Access,
    val membersWithAccess: Map<Member, Access> = emptyMap(),
) {

    val emails: Map<Email, Access> =
        membersWithAccess
            .filter { (member, _) -> member is Email } as Map<Email, Access>

    val users: Map<User, Access> =
        membersWithAccess
            .filter { (member, _) -> member is User } as Map<User, Access>

    val groups: Map<Group, Access> =
        membersWithAccess
            .filter { (member, _) -> member is Group } as Map<Group, Access>
}

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
    private val shareData: ShareData? = null
) : ViewModel() {

    companion object {

        fun initState(
            access: Access,
            users: List<User>,
            groups: List<Group>,
            emails: List<String>,
        ): InviteAccessState {

            fun correctAccess(isAdmin: Boolean): Access {
                return correctAccess(access, isAdmin)
            }

            return InviteAccessState(
                commonAccess = access,
                membersWithAccess = buildMap {
                    putAll(emails.associate { email -> Email(email) to correctAccess(false) })
                    putAll(groups.associateWith { correctAccess(false) })
                    putAll(users.associateWith { user -> correctAccess(user.isAdmin || user.isRoomAdmin) })
                }
            )
        }

        fun correctAccess(access: Access, isAdmin: Boolean): Access {
            return if (access == Access.RoomManager) {
                if (isAdmin) {
                    access
                } else {
                    Access.ContentCreator
                }
            } else {
                access
            }
        }
    }

    private val _state: MutableStateFlow<InviteAccessState> =
        MutableStateFlow(initState(access, users, groups, emails))
    val state: StateFlow<InviteAccessState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<InviteAccessEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<InviteAccessEffect> = _effect.asSharedFlow()

    fun setAccess(emailOrId: String, access: Access) {
        val entry = state.value
            .membersWithAccess
            .entries.find { (member, _) -> member.id == emailOrId } ?: return

        _state.update {
            it.copy(
                membersWithAccess = it.membersWithAccess
                    .toMutableMap()
                    .apply {
                        if (access == Access.None) {
                            remove(entry.key)
                        } else {
                            set(entry.key, access)
                        }
                    }
            )
        }
    }

    fun setAllAccess(access: Access) {
        _state.update {
            it.copy(
                commonAccess = access,
                membersWithAccess = it.membersWithAccess
                    .mapValues { (member, _) ->
                        if (access == Access.RoomManager) {
                            when (member) {
                                is User -> correctAccess(
                                    access,
                                    member.isAdmin || member.isRoomAdmin
                                )

                                else -> correctAccess(access, false)
                            }
                        } else {
                            access
                        }
                    }
            )
        }
    }

    open fun invite() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true) }
                val api = checkNotNull(shareService) { "api can't be null" }
                val shareData = checkNotNull(shareData) { "item id can't be null" }

                val request = RequestShare(
                    share = state.value
                        .membersWithAccess
                        .map { (member, access) ->
                            RequestShareItem(
                                member.id,
                                access.code
                            )
                        },
                )
                if (shareData.isFolder) {
                    api.setFolderAccess(shareData.itemId, request)
                } else {
                    api.setFileAccess(shareData.itemId, request)
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