package app.editors.manager.viewModels.main

import android.content.ContentResolver
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

data class TemplateInfoState(
    val title: String = "",
    val roomType: Int = 0,
    val tags: List<String> = emptyList(),
    val logo: CloudFolderLogo? = null,
    val quota: StorageQuota = StorageQuota(),
    val canApplyChanges: Boolean = false,
    val editAccess: Boolean = false,
    val accessSettings: TemplateAccessSettings = TemplateAccessSettings(),
    val isSaving: Boolean = false
)

fun CloudFolder.asTemplateState() = TemplateInfoState(
    title = title,
    roomType = roomType,
    tags = tags.toList(),
    logo = logo,
    canApplyChanges = true,
    editAccess = security?.editAccess == true,
    quota = StorageQuota.fromBytes(quotaLimit)
)

open class TemplateInfoViewModel(
    contentResolver: ContentResolver,
    private val roomProvider: RoomProvider,
    private val resourceProvider: ResourcesProvider,
    private val mode: TemplateSettingsMode
) : BaseLogoViewModel(contentResolver, roomProvider),
    EventSender by BaseEventSender(resourceProvider) {

    private val _loadingStatus = MutableStateFlow<ResultUi<*>>(ResultUi.Loading)
    val loadingStatus = _loadingStatus.asStateFlow()

    private val _uiState = MutableStateFlow(TemplateInfoState())
    val uiState = _uiState.asStateFlow()

    protected val templateId: String
        get() = mode.templateId

    init {
        loadTemplateInfo()
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
        return if (mode is TemplateSettingsMode.CreateTemplate) {
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

    fun updateAccessSettings(settings: TemplateAccessSettings) {
        _uiState.update { it.copy(accessSettings = settings) }
    }

    open fun save() {
        if (uiState.value.isSaving) return
        viewModelScope.launch {
            updateUiState { it.copy(isSaving = true) }
            createRoomFromTemplate()
        }
    }

    protected suspend fun createRoomFromTemplate() {
        with(uiState.value) {
            performTemplateOperation(
                errorMessage = R.string.error_create_room,
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

    protected suspend fun <T> performTemplateOperation(
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

    protected fun showMessage(msgId: Int) {
        viewModelScope.launch {
            sendMessage(msgId)
        }
    }

    protected fun updateUiState(block: (TemplateInfoState) -> TemplateInfoState) {
        _uiState.update(block)
    }

    fun getCurrentUser(): User = mode.user

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
                TemplateInfoViewModel(
                    contentResolver = app.contentResolver,
                    roomProvider = app.appComponent.roomProvider,
                    resourceProvider = app.appComponent.resourcesProvider,
                    mode = TemplateSettingsMode.fromModeId(modeId, currentUser, templateId)
                )
            }
        }
    }
}