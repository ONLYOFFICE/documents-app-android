package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Group
import app.documents.core.model.login.Member
import app.documents.core.model.login.User
import app.documents.core.network.share.ShareService
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import lib.toolkit.base.managers.tools.ResourcesProvider

class CloudUserListViewModel(
    access: Access?,
    mode: UserListMode,
    resourcesProvider: ResourcesProvider,
    private val invitedIds: List<String>,
    private val shareService: ShareService
) : UserListViewModel(
    access = access,
    mode = mode,
    resourcesProvider = resourcesProvider
) {

    override val cachedMembersFlow: SharedFlow<List<Member>> = flow { emit(getMembers()) }
        .onEach(::updateListState)
        .catch { error -> handleError(error) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private suspend fun getMembers(): List<Member> {
        val groups = shareService.getGroups(getOptions())
            .response
            .checkIfShared(emptyList())

        val users = shareService.getUsers(getOptions())
            .response
            .checkIfShared(groups)

        return groups + users
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Member> List<T>.checkIfShared(groups: List<Group>): List<T> {
        return when (T::class) {
            Group::class -> {
                filterIsInstance<Group>().map { it.copy(shared = it.id in invitedIds) }
            }

            User::class -> {
                val invitedGroups = groups.filter(Group::shared).map(Group::id)
                filterIsInstance<User>()
                    .map { user ->
                        user.copy(
                            shared = user.groups.any { group ->
                                group.id in invitedGroups
                            } || user.id in invitedIds
                        )
                    }
            }

            else -> this
        } as List<T>
    }
}