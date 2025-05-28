package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.login.Group
import app.documents.core.model.login.Member
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.getOrDefault
import app.documents.core.providers.RoomProvider
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.usecase.SaveAccessSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider

data class TemplateAccessSettings(
    val public: Boolean = false,
    val selectedUsers: List<User> = emptyList(),
    val selectedGroups: List<Group> = emptyList()
)

data class TemplateAccessSettingsState(
    val confirmedSettings: TemplateAccessSettings = TemplateAccessSettings(),
    val selectedUsers: List<User> = emptyList(),
    val selectedGroups: List<Group> = emptyList(),
    val loading: Boolean = true,
    val requestLoading: Boolean = false
)

sealed class TemplateUserListEffect : UserListEffect() {
    data object SavedSuccessfully : TemplateUserListEffect()
}

class TemplateUserListViewModel(
    resourcesProvider: ResourcesProvider,
    private val roomProvider: RoomProvider,
    private val saveAccessSettings: SaveAccessSettingsUseCase,
    private val cloudAccount: CloudAccount?,
    private val initSettings: TemplateAccessSettings?,
    private val mode: TemplateSettingsMode
) : UserListViewModel(
    mode = UserListMode.TemplateAccess,
    access = null,
    resourcesProvider = resourcesProvider
) {

    override val cachedMembersFlow: SharedFlow<List<Member>> = flow {
        emit(getMembers())
    }
        .onEach(::updateListState)
        .catch { error -> handleError(error) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val _settings = MutableStateFlow(TemplateAccessSettingsState())
    val settings: StateFlow<TemplateAccessSettingsState> = _settings

    init {
        collectViewState()
    }

    private suspend fun initSettings(users: List<User>, groups: List<Group>) {
        if (initSettings != null) {
            _settings.update { current ->
                current.copy(confirmedSettings = initSettings)
            }
        } else {
            val public = roomProvider.getTemplatePublic(mode.templateId)
                .first()
                .getOrDefault(false)
            _settings.update { current ->
                current.copy(
                    confirmedSettings = TemplateAccessSettings(
                        public = public,
                        selectedUsers = users.filter { it.shared },
                        selectedGroups = groups.filter { it.shared }
                    )
                )
            }
        }
    }

    private fun collectViewState() {
        viewModelScope.launch {
            viewState.collect { state ->
                _settings.update { current ->
                    current.copy(
                        selectedUsers = getSelectedUsers(),
                        selectedGroups = getSelectedGroups(),
                        loading = state.loading
                    )
                }
            }
        }
    }

    private suspend fun getMembers(): List<Member> {
        val templateExists = mode !is TemplateSettingsMode.CreateTemplate
        val groups = if (templateExists) {
            roomProvider.getGroups(mode.templateId, getOptions())
        } else {
            roomProvider.getGroups(mode.templateId, getOptions()).map { it.copy(shared = false) }
        }

        val users = roomProvider.getUsers(mode.templateId, getOptions())
            .filterNot { it.id == mode.user.id }
            .filter { it.status == ApiContract.EmployeeStatus.ACTIVE && (it.isAdmin || it.isRoomAdmin) }
            .map { user ->
                user.copy(
                    avatarMedium = cloudAccount?.portal?.urlWithScheme + user.avatarMedium,
                    shared = if (templateExists) user.shared else false
                )
            }

        initSettings(users, groups)
        return groups + users
    }

    fun getCurrentUser(): User = mode.user

    fun switchPublic(public: Boolean) {
        _settings.update {
            it.copy(confirmedSettings = it.confirmedSettings.copy(public = public))
        }
    }

    fun onSaveClick(settings: TemplateAccessSettings) {
        viewModelScope.launch {
            _settings.update { it.copy(requestLoading = true) }
            saveAccessSettings(
                templateId = mode.templateId,
                settings = settings,
                onError = ::handleError
            ) { emitEffect(TemplateUserListEffect.SavedSuccessfully) }
            _settings.update { it.copy(requestLoading = false) }
        }
    }

    fun onConfirmMembersList() {
        _settings.update { current ->
            current.copy(
                confirmedSettings = current.confirmedSettings.copy(
                    selectedUsers = current.selectedUsers,
                    selectedGroups = current.selectedGroups
                )
            )
        }
    }

    fun deleteMember(memberId: String) {
        toggleSelect(memberId)
        _settings.update { current ->
            current.copy(
                confirmedSettings = current.confirmedSettings.copy(
                    selectedUsers = current.confirmedSettings.selectedUsers.filterNot { it.id == memberId },
                    selectedGroups = current.confirmedSettings.selectedGroups.filterNot { it.id == memberId }
                ),
            )
        }
    }

    fun setSelection() {
        val confirmedSelected = settings.value.confirmedSettings.selectedUsers.map { it.id } +
                settings.value.confirmedSettings.selectedGroups.map { it.id }
        updateState { current ->
            current.copy(selected = confirmedSelected)
        }
    }

    companion object {
        fun factory(
            templateId: String,
            modeId: Int,
            initSettings: TemplateAccessSettings? = null
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as App)
                val accountOnline = app.appComponent.accountOnline
                val currentUser = User(
                    id = accountOnline?.id ?: "",
                    displayName = accountOnline?.name ?: "",
                    avatarMedium = accountOnline?.avatarUrl ?: ""
                )
                TemplateUserListViewModel(
                    roomProvider = app.roomProvider,
                    saveAccessSettings = app.appComponent.saveAccessSettingsUseCase,
                    cloudAccount = app.accountOnline,
                    resourcesProvider = app.appComponent.resourcesProvider,
                    initSettings = initSettings,
                    mode = TemplateSettingsMode.fromModeId(modeId, currentUser, templateId)
                )
            }
        }
    }
}