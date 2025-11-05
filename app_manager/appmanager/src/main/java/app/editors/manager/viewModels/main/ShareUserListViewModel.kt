package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Member
import app.documents.core.providers.RoomProvider
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

class ShareUserListViewModel(
    private val itemId: String,
    private val isFolder: Boolean,
    private val roomProvider: RoomProvider,
    accessList: List<Access>,
) : UserListViewModel(
    mode = UserListMode.Share,
    access = accessList.last { access -> access !is Access.Restrict },
) {

    override val cachedMembersFlow: SharedFlow<List<Member>> = flow {
        emit(getMembers())
    }
        .onEach(::updateListState)
        .catch { error -> handleError(error) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private suspend fun getMembers(): List<Member> {
        val portal = App.getApp().accountOnline?.portal?.urlWithScheme

        val groups = roomProvider.getGroupsByItemId(itemId, isFolder)

        val users = roomProvider.getUsersByItemId(itemId, isFolder)
            .map { user -> user.copy(avatarMedium = portal + user.avatarMedium) }

        val guests = roomProvider.getGuestsByItemId(itemId, isFolder)
            .filter { it.status != 4 }
            .map { user -> user.copy(avatarMedium = portal + user.avatarMedium) }

        return groups + users + guests
    }
}