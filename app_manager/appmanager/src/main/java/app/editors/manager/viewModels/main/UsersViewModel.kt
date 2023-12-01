package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.documents.core.network.login.models.User
import app.documents.core.network.share.ShareService
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.mutableStateIn

class UserViewModelFactory(
    private val id: String,
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
    private val id: String,
    private val shareService: ShareService,
    private val roomProvider: RoomProvider,
    private val resourcesProvider: ResourcesProvider) : ViewModel() {

    private val _viewState: MutableStateFlow<UsersViewState> = MutableStateFlow(UsersViewState.None)
    val viewState: StateFlow<UsersViewState> = _viewState

    private val _usersFlow: MutableStateFlow<List<User>> = flow {
        _viewState.emit(UsersViewState.Loading)
        emit(shareService.getUsers().response.filter { !it.isOwner }.sortedBy { it.displayName })
        _viewState.emit(UsersViewState.None)
    }.mutableStateIn(viewModelScope, emptyList())
    val usersFlow: StateFlow<List<User>> = _usersFlow

    private var filterJob: Job? = null

    fun search(string: String) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            try {
                delay(200)
                _viewState.emit(UsersViewState.Loading)
                if (string.isEmpty()) {
                    _usersFlow.emit(shareService.getUsers().response.filter { !it.isOwner }.sortedBy { it.displayName })
                } else {
                    _usersFlow.emit(shareService.getUsers().response.filter {
                        !it.isOwner && it.displayName.contains(
                            string,
                            true
                        )
                    }.sortedBy { it.displayName })
                }
                _viewState.emit(UsersViewState.None)
            } catch (error: Throwable) {
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
                roomProvider.setRoomOwner(id, userId)
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

}