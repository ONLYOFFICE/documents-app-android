package app.editors.manager.viewModels.main

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.network.common.Result
import app.documents.core.network.common.getOrDefault
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.CloudFolderLogo
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.tools.BaseEvent
import app.editors.manager.managers.tools.BaseEventSender
import app.editors.manager.managers.tools.EventSender
import app.editors.manager.managers.usecase.SaveAccessSettingsUseCase
import app.editors.manager.mvp.models.ui.ResultUi
import app.editors.manager.mvp.models.ui.StorageQuota
import app.editors.manager.mvp.models.ui.networkAsFlowResultUI
import app.editors.manager.viewModels.base.BaseLogoViewModel
import app.editors.manager.viewModels.base.RoomSettingsLogoState
import app.editors.manager.viewModels.base.UploadedRoomLogo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.tools.ResourcesProvider

sealed interface TemplateSettingsEvent : BaseEvent {
    data class Created(val id: String) : TemplateSettingsEvent
    data object Edited : TemplateSettingsEvent
}

sealed class TemplateSettingsMode(
    val user: User,
    val templateId: String
) {
    class CreateTemplate(user: User, roomId: String) : TemplateSettingsMode(user, roomId)
    class EditTemplate(user: User, templateId: String) : TemplateSettingsMode(user, templateId)
    class CreateRoom(user: User, templateId: String) : TemplateSettingsMode(user, templateId)

    companion object {
        const val MODE_CREATE_TEMPLATE = 1
        const val MODE_EDIT_TEMPLATE = 2
        const val MODE_CREATE_ROOM = 3

        fun fromModeId(modeId: Int, user: User, templateId: String): TemplateSettingsMode {
            return when (modeId) {
                MODE_CREATE_TEMPLATE -> CreateTemplate(user, templateId)
                MODE_EDIT_TEMPLATE -> EditTemplate(user, templateId)
                MODE_CREATE_ROOM -> CreateRoom(user, templateId)
                else -> throw IllegalArgumentException("Unknown mode ID: $modeId")
            }
        }
    }
}

data class TemplateSettingsState(
    val title: String = "",
    val roomType: Int = 0,
    val tags: List<String> = emptyList(),
    val logo: CloudFolderLogo? = null,
    val quota: StorageQuota = StorageQuota(),
    val canApplyChanges: Boolean = false,
    val accessSettings: TemplateAccessSettings = TemplateAccessSettings(),
    val isSaving: Boolean = false
)

fun CloudFolder.asTemplateState() = TemplateSettingsState(
    title = title,
    roomType = roomType,
    tags = tags.toList(),
    logo = logo,
    canApplyChanges = true,
    quota = StorageQuota.fromBytes(quotaLimit)
)

class TemplateSettingsViewModel(
    contentResolver: ContentResolver,
    private val roomProvider: RoomProvider,
    private val resourceProvider: ResourcesProvider,
    private val saveAccessSettings: SaveAccessSettingsUseCase,
    private val mode: TemplateSettingsMode
) : BaseLogoViewModel(contentResolver, roomProvider),
    EventSender by BaseEventSender(resourceProvider) {

    private val _loadingStatus = MutableStateFlow<ResultUi<*>>(ResultUi.Loading)
    val loadingStatus = _loadingStatus.asStateFlow()

    private val _uiState = MutableStateFlow(TemplateSettingsState())
    val uiState = _uiState.asStateFlow()

    private val templateId: String
        get() = mode.templateId

    init {
        loadTemplateInfo()
    }

    fun deleteLogo() {
        super.setLogoUri(null)
        if (logoState.value.logoWebUrl?.isNotEmpty() == true) {
            updateLogoState { it.copy(logoWebUrl = null) }
        }
    }

    private fun loadTemplateInfo() {
        viewModelScope.launch {
            roomProvider.getRoomInfo(templateId)
                .networkAsFlowResultUI { it.asTemplateState() }
                .collect { result ->
                    if (result is ResultUi.Success) {
                        val settings = loadAccessSettings()
                        _uiState.update { result.data.copy(accessSettings = settings) }
                        updateLogoState {
                            RoomSettingsLogoState(
                                logoWebUrl = result.data.logo?.medium
                                    ?.takeIf { it.isNotEmpty() }
                            )
                        }
                    }
                    _loadingStatus.update { result }
                }
        }
    }

    private suspend fun loadAccessSettings(): TemplateAccessSettings {
        return if (mode !is TemplateSettingsMode.EditTemplate) {
            TemplateAccessSettings()
        } else {
            combine(
                roomProvider.getTemplateMembers(templateId),
                roomProvider.getTemplatePublic(templateId)
            ) { membersResult, publicResult ->
                val members = membersResult.getOrDefault(emptyList())
                val public = publicResult.getOrDefault(false)
                val (userShares, groupShares) = members.partition { it.sharedTo.name.isEmpty() }

                TemplateAccessSettings(
                    selectedUsers = userShares
                        .filter { it.sharedTo.id != mode.user.id }
                        .map { share ->
                            User(
                                id = share.sharedTo.id,
                                displayName = share.sharedTo.displayName,
                                avatarMedium = share.sharedTo.avatarMedium
                            )
                        },
                    selectedGroups = groupShares.map { share ->
                        Group(
                            id = share.sharedTo.id,
                            name = share.sharedTo.name
                        )
                    },
                    public = public
                )
            }.first()
        }
    }

    fun addOrRemoveTag(tag: String) {
        with(_uiState.value) {
            val updatedTags = if (tag in tags) {
                tags - tag
            } else {
                tags + tag
            }
            _uiState.update { it.copy(tags = updatedTags) }
        }
    }

    override fun setLogoUri(uri: Uri?) {
        _uiState.update { it.copy(canApplyChanges = false) }
        viewModelScope.launch(Dispatchers.IO) {
            super.setLogoUri(uri)
            _uiState.update { it.copy(canApplyChanges = true) }
        }
    }

    fun save() {
        if (uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (mode) {
                is TemplateSettingsMode.CreateTemplate -> createTemplate()
                is TemplateSettingsMode.EditTemplate -> editTemplate()
                is TemplateSettingsMode.CreateRoom -> createRoomFromTemplate()
            }
        }
    }

    private suspend fun createTemplate() {
        with(uiState.value) {
            performTemplateOperation(
                errorMessage = R.string.error_create_template,
                successHandler = { sendEvent(TemplateSettingsEvent.Created(it)) }
            ) { uploadedLogo, copyLogo ->
                roomProvider.createTemplate(
                    title = title,
                    tags = tags,
                    quota = quota.takeIf(StorageQuota::enabled)?.bytes ?: -1,
                    roomId = templateId,
                    public = accessSettings.public,
                    copyLogo = copyLogo,
                    color = logo?.color ?: "",
                    logoUrl = uploadedLogo?.tmpFile,
                    logoSize = uploadedLogo?.size,
                    share = accessSettings.selectedUsers.map { it.email ?: "" },
                    groups = accessSettings.selectedGroups.map { it.id }
                )
            }
        }
    }

    private suspend fun editTemplate() {
        if (logoState.value.logoUri == null && logoState.value.logoWebUrl == null) {
            roomProvider.deleteLogo(templateId)
        }

        with(uiState.value) {
            performTemplateOperation(
                errorMessage = R.string.error_save_template,
                successHandler = {
                    saveAccessSettings(
                        templateId = templateId,
                        settings = accessSettings,
                        onError = { showMessage(R.string.error_save_access_settings) },
                        onSuccess = { sendEvent(TemplateSettingsEvent.Edited) }
                    )
                }
            ) { uploadedLogo, _ ->
                roomProvider.editTemplate(
                    id = templateId,
                    newTitle = title,
                    quota = quota.takeIf(StorageQuota::enabled)?.bytes ?: -1,
                    tags = tags,
                    logoSize = uploadedLogo?.size,
                    logoUrl = uploadedLogo?.tmpFile
                )
            }
        }
    }

    private suspend fun createRoomFromTemplate() {
        with(uiState.value) {
            performTemplateOperation(
                errorMessage = R.string.error_create_template,
                successHandler = { sendEvent(TemplateSettingsEvent.Created(it)) }
            ) { uploadedLogo, copyLogo ->
                roomProvider.createRoomFromTemplate(
                    templateId = templateId,
                    title = title,
                    tags = tags,
                    quota = quota.takeIf(StorageQuota::enabled)?.bytes ?: -1,
                    copyLogo = copyLogo,
                    color = logo?.color ?: "",
                    logoUrl = uploadedLogo?.tmpFile,
                    logoSize = uploadedLogo?.size,
                )
            }
        }
    }

    private suspend fun <T> performTemplateOperation(
        errorMessage: Int,
        successHandler: suspend (T) -> Unit,
        operation: suspend (UploadedRoomLogo?, Boolean) -> Flow<Result<T>>
    ) {
        val uploadedRoomLogo = withContext(Dispatchers.IO) {
            getUploadedRoomLogo { showMessage(R.string.error_upload_logo) }
        }
        val copyLogo = with(logoState.value) { logoPreview == null && logoWebUrl != null }
        uploadedRoomLogo.let { uploadedLogo ->
            operation(uploadedLogo, copyLogo).collect { result ->
                when (result) {
                    is Result.Error -> {
                        _uiState.update { it.copy(isSaving = false, canApplyChanges = true) }
                        showMessage(errorMessage)
                    }

                    is Result.Success -> successHandler(result.result)
                }
            }
        }
    }

    fun updateAccessSettings(settings: TemplateAccessSettings) {
        _uiState.update { it.copy(accessSettings = settings) }
    }

    fun changeTitle(title: String) {
        _uiState.update { it.copy(title = title, canApplyChanges = title.isNotBlank()) }
    }

    fun updateStorageQuota(block: (StorageQuota) -> StorageQuota) {
        _uiState.update { it.copy(quota = block(it.quota)) }
    }

    private fun showMessage(msgId: Int) {
        viewModelScope.launch {
            sendMessage(msgId)
        }
    }

    fun getMode(): TemplateSettingsMode = mode

    companion object {
        fun factory(
            templateId: String,
            modeId: Int
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as App)
                val accountOnline = app.appComponent.accountOnline
                val currentUser = User(
                    id = accountOnline?.id ?: "",
                    displayName = accountOnline?.name ?: "",
                    avatarMedium = accountOnline?.avatarUrl ?: ""
                )
                TemplateSettingsViewModel(
                    contentResolver = app.contentResolver,
                    resourceProvider = app.appComponent.resourcesProvider,
                    roomProvider = app.appComponent.roomProvider,
                    saveAccessSettings = app.appComponent.saveAccessSettingsUseCase,
                    mode = TemplateSettingsMode.fromModeId(modeId, currentUser, templateId)
                )
            }
        }
    }
}