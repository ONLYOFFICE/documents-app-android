package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.Group
import app.documents.core.model.login.Member
import app.documents.core.model.login.User
import app.documents.core.network.manager.models.explorer.AccessTarget
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
import kotlinx.coroutines.launch

class RoomUserListViewModel(
    private val roomId: String,
    private val mode: UserListMode,
    private val roomProvider: RoomProvider,
    private val roomOwnerId: String = "",
    roomType: Int? = null,
) : UserListViewModel(
    mode = mode,
    access = ShareData(roomType = roomType).getAccessList(AccessTarget.User).lastOrNull()?.access,
) {

    override val cachedMembersFlow: SharedFlow<List<Member>> = flow { emit(getMembers()) }
        .onEach(::updateListState)
        .catch { error -> handleError(error) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val portal: String? by lazy { App.getApp().accountOnline?.portal?.urlWithScheme }

    private suspend fun getMembers(): List<Member> {
        return getGroups() + getUsers() + getGuests()
    }

    private suspend fun getUsers(): List<User> {
        return when (mode) {
            is UserListMode.Share -> {
                roomProvider.getUsersByItemId(mode.shareData.itemId, mode.shareData.isFolder)
            }

            else -> {
                roomProvider.getUsers(roomId, getOptions())
                    .run {
                        when (mode) {
                            UserListMode.ChangeOwner -> filter { it.isAdmin && it.id != roomOwnerId }
                            UserListMode.StartFilling -> filter { it.shared }
                            else -> this
                        }
                    }

            }
        }.map { user -> user.copy(avatarMedium = portal + user.avatarMedium) }
    }

    private suspend fun getGroups(): List<Group> {
        return when (mode) {
            is UserListMode.Share -> {
                roomProvider.getGroupsByItemId(mode.shareData.itemId, mode.shareData.isFolder)
            }

            UserListMode.ChangeOwner,
            UserListMode.StartFilling -> {
                emptyList()
            }

            else -> roomProvider.getGroups(roomId, getOptions())
        }
    }

    private suspend fun getGuests(): List<User> {
        return when (mode) {
            is UserListMode.Share -> {
                roomProvider.getGuestsByItemId(mode.shareData.itemId, mode.shareData.isFolder)
            }

            else -> {
                roomProvider.getGuests(roomId, getOptions())
                    .run {
                        when (mode) {
                            UserListMode.StartFilling -> filter { it.shared }
                            else -> this
                        }
                    }
            }
        }
            .filter { it.status != 4 }
            .map { user -> user.copy(avatarMedium = portal + user.avatarMedium) }
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