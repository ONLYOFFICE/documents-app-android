package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.Group
import app.documents.core.model.login.Member
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.ShareService
import app.documents.core.utils.displayNameFromHtml
import app.editors.manager.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider
import retrofit2.HttpException
import kotlin.time.Duration.Companion.milliseconds

data class UserListState(
    val loading: Boolean = true,
    val requestLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val groups: List<Group> = emptyList(),
    val access: Int? = null,
    val selected: List<String> = emptyList(),
)

sealed class UserListEffect {
    data class Success(val user: User) : UserListEffect()
    data class Error(val message: String) : UserListEffect()
}

@OptIn(FlowPreview::class)
open class UserListViewModel(
    access: Int?,
    private val shareService: ShareService,
    private val resourcesProvider: ResourcesProvider,
) : ViewModel() {

    private val _viewState: MutableStateFlow<UserListState> = MutableStateFlow(UserListState(access = access))
    val viewState: StateFlow<UserListState> = _viewState

    private val _effect: MutableSharedFlow<UserListEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<UserListEffect> = _effect.asSharedFlow()

    private val searchFlow: MutableSharedFlow<String> = MutableSharedFlow(1)

    protected open var cachedMembersFlow: SharedFlow<List<Member>> = flow {
        val users = shareService.getUsers(getOptions()).response
        val groups = shareService.getGroups(getOptions()).response
        emit(users + groups)
    }
        .catch { error -> handleError(error) }
        .onCompletion { updateListState() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    protected fun handleError(error: Throwable) {
        val message = when (error) {
            is HttpException -> resourcesProvider.getString(R.string.errors_client_error) + error.code()
            else -> resourcesProvider.getString(R.string.errors_unknown_error)
        }
        _effect.tryEmit(UserListEffect.Error(message))
    }

    init {
        collectSearchFlow()
    }

    private fun collectSearchFlow() {
        viewModelScope.launch {
            searchFlow
                .onStart { emit("") }
                .distinctUntilChanged()
                .debounce(500.milliseconds)
                .collect(::updateListState)
        }
    }

    protected fun updateListState(searchValue: String = "") {
        viewModelScope.launch {
            cachedMembersFlow.replayCache.lastOrNull()?.let { cachedMembers ->
                val users = cachedMembers.filterIsInstance<User>().filter {
                    it.displayNameFromHtml.startsWith(searchValue, true)
                }
                val groups = cachedMembers.filterIsInstance<Group>().filter {
                    it.name.startsWith(searchValue, true)
                }
                _viewState.update { it.copy(loading = false, users = users, groups = groups) }
            }
        }
    }

    fun search(searchValue: String) {
        searchFlow.tryEmit(searchValue)
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

    fun setAccess(access: Int) {
        _viewState.update { it.copy(access = access) }
    }

    fun onDelete() {
        _viewState.update { it.copy(selected = emptyList()) }
    }

    fun getSelectedUsers(): List<User> {
        return _viewState.value.users.filter { _viewState.value.selected.contains(it.id) }
    }

    fun getSelectedGroups(): List<Group> {
        return _viewState.value.groups.filter { _viewState.value.selected.contains(it.id) }
    }

    protected fun emitEffect(effect: UserListEffect) {
        _effect.tryEmit(effect)
    }

    protected fun updateState(block: (UserListState) -> UserListState) {
        _viewState.update(block)
    }

    protected fun getOptions(): Map<String, String> = mapOf(
        ApiContract.Parameters.ARG_SORT_BY to ApiContract.Parameters.VAL_SORT_BY_FIRST_NAME,
        ApiContract.Parameters.ARG_SORT_ORDER to ApiContract.Parameters.VAL_SORT_ORDER_ASC,
        ApiContract.Parameters.ARG_FILTER_OP to ApiContract.Parameters.VAL_FILTER_OP_CONTAINS
    )
}