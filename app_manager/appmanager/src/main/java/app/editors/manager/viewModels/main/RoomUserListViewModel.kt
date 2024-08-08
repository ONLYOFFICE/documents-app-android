package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.Group
import app.documents.core.model.login.Member
import app.documents.core.model.login.User
import app.documents.core.network.share.ShareService
import app.documents.core.providers.RoomProvider
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.RoomUtils
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider

sealed class UserListMode {
    data object Invite : UserListMode()
    data object ChangeOwner : UserListMode()
}

class RoomUserListViewModel(
    roomType: Int? = null,
    private val roomId: String,
    private val roomOwnerId: String = "",
    private val mode: UserListMode,
    private val shareService: ShareService,
    private val roomProvider: RoomProvider,
    private val resourcesProvider: ResourcesProvider,
) : UserListViewModel(
    access = roomType?.let { RoomUtils.getAccessOptions(it, false).lastOrNull() },
    shareService = shareService,
    resourcesProvider = resourcesProvider
) {

    override var cachedMembersFlow: SharedFlow<List<Member>> =
        flow { emit(getUsers() + getGroups()) }
            .catch { error -> handleError(error) }
            .onCompletion { updateListState() }
            .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private suspend fun getUsers(): List<User> {
        return shareService.getRoomUsers(roomId, getOptions())
            .response
            .filterNot {
                when (mode) {
                    UserListMode.Invite -> it.isOwner
                    UserListMode.ChangeOwner -> it.isVisitor || it.id == roomOwnerId
                }
            }
            .map {
                it.copy(
                    avatarMedium = App.getApp().accountOnline?.portal?.urlWithScheme +
                            it.avatarMedium
                )
            }
    }

    private suspend fun getGroups(): List<Group> {
        return shareService.getRoomGroups(roomId, getOptions()).response
    }

    fun setOwner(userId: String, leave: Boolean) {
        viewModelScope.launch {
            updateState { it.copy(requestLoading = true) }
            try {
                val room = roomProvider.setRoomOwner(roomId, userId)
                if (leave) {
                    roomProvider.leaveRoom(roomId, App.getApp().accountOnline?.id.orEmpty())
                }
                emitEffect(
                    UserListEffect.Success(
                        User(
                            id = room.createdBy.id,
                            displayName = room.createdBy.displayName
                        )
                    )
                )
            } catch (error: Throwable) {
                handleError(error)
            } finally {
                updateState { it.copy(requestLoading = false) }
            }
        }
    }
}