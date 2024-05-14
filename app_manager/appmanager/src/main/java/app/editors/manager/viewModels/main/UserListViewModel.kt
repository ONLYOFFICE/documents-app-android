package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.share.ShareService
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider

data class UserListState(
    val loading: Boolean = true,
    val requestLoading: Boolean = false,
    val users: List<Pair<User, Boolean>> = emptyList() // true if selected
)

sealed class UserListEffect {
    data object Success : UserListEffect()
    data class Error(val message: String) : UserListEffect()
}

class UserListViewModel(
    private val item: CloudFolder,
    private val shareService: ShareService,
    private val roomProvider: RoomProvider,
    private val resourcesProvider: ResourcesProvider
) : ViewModel() {

    private var filterJob: Job? = null

    private val _viewState: MutableStateFlow<UserListState> = MutableStateFlow(UserListState())
    val viewState: StateFlow<UserListState> = _viewState

    private val _effect: MutableSharedFlow<UserListEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<UserListEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            fetchUsers()
        }
    }

    private suspend fun fetchUsers(searchValue: String = "") {
        val users = shareService.getUsers(getOptions(searchValue))
            .response
            .filter { !it.isOwner }
            .map { it.copy(avatarMedium = App.getApp().accountOnline?.portal?.urlWithScheme + it.avatarMedium) }

        _viewState.update { it.copy(loading = false, users = users.map { user -> user to false }) }
    }

    fun search(searchValue: String) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300L)
            try {
                fetchUsers(searchValue)
            } catch (_: CancellationException) {
                //
            } catch (error: Throwable) {
                val errorMessage = error.message ?: resourcesProvider.getString(R.string.errors_unknown_error)
                _effect.tryEmit(UserListEffect.Error(errorMessage))
            }
        }
    }

    fun setOwner(userId: String) {
        viewModelScope.launch {
            _viewState.update { it.copy(requestLoading = true) }
            try {
                roomProvider.setRoomOwner(item.id, userId, App.getApp().accountOnline?.id ?: "")
                _effect.emit(UserListEffect.Success)
            } catch (error: Throwable) {
                val errorMessage = error.message ?: resourcesProvider.getString(R.string.errors_unknown_error)
                _effect.emit(UserListEffect.Error(errorMessage))
            } finally {
                _viewState.update { it.copy(requestLoading = false) }
            }
        }
    }

    private fun getOptions(searchValue: String): Map<String, String> = mapOf(
        ApiContract.Parameters.ARG_FILTER_VALUE to searchValue,
        ApiContract.Parameters.ARG_SORT_BY to ApiContract.Parameters.VAL_SORT_BY_FIRST_NAME,
        ApiContract.Parameters.ARG_SORT_ORDER to ApiContract.Parameters.VAL_SORT_ORDER_ASC,
        ApiContract.Parameters.ARG_FILTER_OP to ApiContract.Parameters.VAL_FILTER_OP_CONTAINS
    )

}