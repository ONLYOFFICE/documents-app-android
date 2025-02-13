package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.launch

class RoomInviteAccessViewModel(
    private val roomId: String,
    private val roomProvider: RoomProvider,
    access: Access,
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
                        roomId = roomId,
                        emails = state.value
                            .emails
                            .map { (email, access) -> email.id to access.code }
                            .toMap()
                    )
                } else {
                    roomProvider.inviteById(
                        roomId = roomId,
                        users = (state.value.users + state.value.groups)
                            .map { (member, access) -> member.id to access.code }
                            .toMap()
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