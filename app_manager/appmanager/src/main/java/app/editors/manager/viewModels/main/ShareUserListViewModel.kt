package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Member
import app.documents.core.providers.RoomProvider
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.tools.ShareData
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

class ShareUserListViewModel(
    private val roomProvider: RoomProvider,
    private val shareData: ShareData,
    currentAccess: Access,
) : UserListViewModel(
    mode = UserListMode.Share(shareData),
    access = currentAccess,
) {

    override val cachedMembersFlow: SharedFlow<List<Member>> = flow { emit(getMembers()) }
        .onEach(::updateListState)
        .catch { error -> handleError(error) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private suspend fun getMembers(): List<Member> {
        val portal = App.getApp().accountOnline?.portal?.urlWithScheme

        val groups = roomProvider.getGroupsByItemId(shareData.itemId, shareData.isFolder)

        val users = roomProvider.getUsersByItemId(shareData.itemId, shareData.isFolder)
            .map { user -> user.copy(avatarMedium = portal + user.avatarMedium) }

        val guests = roomProvider.getGuestsByItemId(shareData.itemId, shareData.isFolder)
            .filter { it.status != 4 }
            .map { user -> user.copy(avatarMedium = portal + user.avatarMedium) }

        return groups + users + guests
    }
}