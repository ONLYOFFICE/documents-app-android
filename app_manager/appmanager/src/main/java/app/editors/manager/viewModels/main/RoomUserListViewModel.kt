package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.Member
import app.documents.core.model.login.User
import app.documents.core.providers.RoomProvider
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.RoomUtils
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider

class RoomUserListViewModel(
    private val roomId: String,
    private val mode: UserListMode,
    private val roomProvider: RoomProvider,
    private val roomOwnerId: String = "",
    roomType: Int? = null,
    resourcesProvider: ResourcesProvider,
) : UserListViewModel(
    mode = mode,
    access = roomType?.let { RoomUtils.getAccessOptions(it, false).lastOrNull() },
    resourcesProvider = resourcesProvider
) {

    override val cachedMembersFlow: SharedFlow<List<Member>> = flow {
        emit(getMembers())
    }
        .onEach(::updateListState)
        .catch { error -> handleError(error) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private suspend fun getMembers(): List<Member> {
        val portal = App.getApp().accountOnline?.portal?.urlWithScheme

        val groups = when (mode) {
            UserListMode.ChangeOwner,
            UserListMode.StartFilling -> emptyList()
            else -> roomProvider.getGroups(roomId, getOptions())
        }

        val users = roomProvider.getUsers(roomId, getOptions())
            .run {
                when (mode) {
                    UserListMode.ChangeOwner -> filter { it.isAdmin && it.id != roomOwnerId }
                    UserListMode.StartFilling -> filter { it.shared }
                    else -> this
                }
            }
            .map { user -> user.copy(avatarMedium = portal + user.avatarMedium) }

        val guests = roomProvider.getGuests(roomId, getOptions())
            .run {
                when (mode) {
                    UserListMode.StartFilling -> filter { it.shared }
                    else -> this
                }
            }
            .filter { it.status != 4 }
            .map { user -> user.copy(avatarMedium = portal + user.avatarMedium) }

        return groups + users + guests
    }

    fun refreshMembers() {

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