package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.share.ShareService
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.mutableStateIn

class UserViewModelFactory(
    private val id: CloudFolder,
    private val shareService: ShareService,
    private val roomProvider: RoomProvider,
    private val resourcesProvider: ResourcesProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UsersViewModel(id, shareService, roomProvider, resourcesProvider) as T
    }
}

sealed class UsersViewState {
    data object None : UsersViewState()
    data object Loading : UsersViewState()
    data object Success : UsersViewState()
    class Error(val message: String) : UsersViewState()
}

class UsersViewModel(
    private val item: CloudFolder,
    private val shareService: ShareService,
    private val roomProvider: RoomProvider,
    private val resourcesProvider: ResourcesProvider
) : ViewModel() {

    private val _viewState: MutableStateFlow<UsersViewState> = MutableStateFlow(UsersViewState.None)
    val viewState: StateFlow<UsersViewState> = _viewState

    private val _usersFlow: MutableStateFlow<List<User>> = flow {
        try {
            _viewState.emit(UsersViewState.Loading)
            emit(shareService.getUsers(getOptions()).response.filter { !it.isOwner }
                .map { it.copy(avatarMedium = ApiContract.SCHEME_HTTPS + App.getApp().appComponent.networkSettings.getPortal() + it.avatarMedium) })
            _viewState.emit(UsersViewState.None)
        } catch (error: Throwable) {
            emit(emptyList())
        }
    }.mutableStateIn(viewModelScope, emptyList())
    val usersFlow: StateFlow<List<User>> = _usersFlow

    private var filterJob: Job? = null

    fun search(string: String) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300L)
            try {
                _viewState.emit(UsersViewState.Loading)
                _usersFlow.emit(shareService.getUsers(getOptions(string)).response.filter { !it.isOwner })
                _viewState.emit(UsersViewState.None)
            } catch (error: Throwable) {
                if (filterJob?.isCancelled == true) return@launch
                _viewState.emit(
                    UsersViewState.Error(
                        error.message ?: resourcesProvider.getString(R.string.errors_unknown_error)
                    )
                )
            }
        }
    }

    fun setOwner(userId: String) {
        viewModelScope.launch {
            _viewState.emit(UsersViewState.Loading)
            try {
                roomProvider.setRoomOwner(item.id, userId, App.getApp().accountOnline?.id ?: "")
                _viewState.emit(UsersViewState.Success)
            } catch (error: Throwable) {
                _viewState.emit(
                    UsersViewState.Error(
                        error.message ?: resourcesProvider.getString(R.string.errors_unknown_error)
                    )
                )
            }
        }
    }

    private fun getOptions(value: String = ""): Map<String, String> = mapOf(
        ApiContract.Parameters.ARG_FILTER_VALUE to value,
        ApiContract.Parameters.ARG_SORT_BY to ApiContract.Parameters.VAL_SORT_BY_FIRST_NAME,
        ApiContract.Parameters.ARG_SORT_ORDER to ApiContract.Parameters.VAL_SORT_ORDER_ASC,
        ApiContract.Parameters.ARG_FILTER_OP to ApiContract.Parameters.VAL_FILTER_OP_CONTAINS
    )

}