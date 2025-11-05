package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.launch

class ShareAccessViewModel(
    private val itemId: String,
    private val roomProvider: RoomProvider,
    private val isFolder: Boolean,
    access: Access,
    users: List<User>,
    groups: List<Group>,
    emails: List<String>
) : InviteAccessViewModel(access, users, groups, emails) {

    override fun invite() {
        viewModelScope.launch {
            try {
                updateState { it.copy(loading = true) }
                roomProvider.setItemShare(
                    itemId = itemId,
                    isFolder = isFolder,
                    members = state.value.let { it.users + it.groups }
                )
                emitEffect(InviteAccessEffect.Success)
            } catch (e: Exception) {
                emitEffect(InviteAccessEffect.Error(e))
            } finally {
                updateState { it.copy(loading = false) }
            }
        }
    }
}