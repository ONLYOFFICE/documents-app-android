package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.RoomGroup
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.ShareService
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.RoomUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import kotlin.time.Duration.Companion.milliseconds

data class UserListState(
    val loading: Boolean = true,
    val requestLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val groups: List<RoomGroup> = emptyList(),
    val access: Int? = null,
    val selected: List<String> = emptyList()
)

sealed class UserListEffect {
    data object Success : UserListEffect()
    data class Error(val message: String) : UserListEffect()
}

@OptIn(FlowPreview::class)
class UserListViewModel(
    private val roomId: String,
    roomType: Int? = null,
    private val shareService: ShareService,
    private val roomProvider: RoomProvider,
    private val resourcesProvider: ResourcesProvider
) : ViewModel() {

    private val _viewState: MutableStateFlow<UserListState> = MutableStateFlow(
        UserListState(
            access = roomType?.let { RoomUtils.getAccessOptions(it, false).lastOrNull() }
        )
    )
    val viewState: StateFlow<UserListState> = _viewState

    private val _effect: MutableSharedFlow<UserListEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<UserListEffect> = _effect.asSharedFlow()

    private val filterFlow: MutableSharedFlow<String> = MutableSharedFlow()

    init {
        viewModelScope.launch {
            filterFlow
                .onStart { emit("") }
                .distinctUntilChanged()
                .debounce(500.milliseconds)
                .collect { searchValue ->
                    try {
                        val users = async {
                            shareService.getRoomUsers(roomId, getOptions(searchValue))
                                .response
                                .filter { !it.isOwner }
                                .map { it.copy(avatarMedium = App.getApp().accountOnline?.portal?.urlWithScheme + it.avatarMedium) }
                        }

                        val groups = async { shareService.getRoomGroups(roomId, getOptions(searchValue)).response }

                        _viewState.update { it.copy(loading = false, users = users.await(), groups = groups.await()) }
                    } catch (error: Throwable) {
                        val errorMessage = error.message ?: resourcesProvider.getString(R.string.errors_unknown_error)
                        _effect.emit(UserListEffect.Error(errorMessage))
                    }
                }
        }
    }

    fun search(searchValue: String) {
        filterFlow.tryEmit(searchValue)
    }

    fun toggleSelect(userId: String) {
        val selected = ArrayList(_viewState.value.selected)
        if (selected.contains(userId)) {
            selected.remove(userId)
        } else {
            selected.add(userId)
        }
        _viewState.update { it.copy(selected = selected) }
    }

    fun setOwner(userId: String) {
        viewModelScope.launch {
            _viewState.update { it.copy(requestLoading = true) }
            try {
                roomProvider.setRoomOwner(roomId, userId, App.getApp().accountOnline?.id ?: "")
                _effect.emit(UserListEffect.Success)
            } catch (error: Throwable) {
                val errorMessage = error.message ?: resourcesProvider.getString(R.string.errors_unknown_error)
                _effect.emit(UserListEffect.Error(errorMessage))
            } finally {
                _viewState.update { it.copy(requestLoading = false) }
            }
        }
    }

    fun setAccess(access: Int) {
        _viewState.update { it.copy(access = access) }
    }

    fun onDelete() {
        _viewState.update { it.copy(selected = emptyList()) }
    }

    fun onInvite() {
        viewModelScope.launch {
            _viewState.update { it.copy(requestLoading = true) }
            try {
                roomProvider.inviteByUserId(
                    roomId = roomId,
                    userIds = _viewState.value.selected,
                    access = _viewState.value.access ?: throw NullPointerException()
                )
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