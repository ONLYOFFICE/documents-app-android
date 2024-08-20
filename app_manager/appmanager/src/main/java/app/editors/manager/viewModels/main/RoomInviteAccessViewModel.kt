package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.launch

class RoomInviteAccessViewModel(
    private val roomId: String,
    private val roomProvider: RoomProvider,
    access: Int,
    users: List<User>,
    groups: List<Group>,
    emails: List<String>
) : InviteAccessViewModel(access, users, groups, emails) {

    override fun invite() {
        viewModelScope.launch {
            try {
                updateState { it.copy(loading = true) }
                if (state.value.emails.isNotEmpty()) {
                    roomProvider.inviteByEmail(
                        roomId,
                        state.value.emails.associateWith { state.value.idAccessList[it] ?: 2 }
                    )
                } else {
                    roomProvider.inviteById(
                        roomId,
                        state.value.users
                            .map(User::id)
                            .plus(state.value.groups.map(Group::id))
                            .associateWith { state.value.idAccessList[it] ?: 2 }
                    )
                }
                emitEffect(InviteAccessEffect.Success)
            } catch (e: Exception) {
                emitEffect(InviteAccessEffect.Error(e))
            } finally {
                updateState { it.copy(loading = false) }
            }
        }
    }
}