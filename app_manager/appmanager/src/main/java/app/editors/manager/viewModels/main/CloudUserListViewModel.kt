package app.editors.manager.viewModels.main

import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.Member
import app.documents.core.network.share.ShareService
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import lib.toolkit.base.managers.tools.ResourcesProvider

class CloudUserListViewModel(
    access: Int?,
    mode: UserListMode,
    invitedIds: List<String>,
    resourcesProvider: ResourcesProvider,
    private val shareService: ShareService
) : UserListViewModel(
    access = access,
    mode = mode,
    invitedIds = invitedIds,
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
}