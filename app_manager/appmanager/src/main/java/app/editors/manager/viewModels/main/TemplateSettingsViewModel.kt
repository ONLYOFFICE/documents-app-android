package app.editors.manager.viewModels.main

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.documents.core.model.login.User
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.usecase.SaveAccessSettingsUseCase
import app.editors.manager.mvp.models.ui.StorageQuota
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider

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

class TemplateSettingsViewModel(
    contentResolver: ContentResolver,
    private val roomProvider: RoomProvider,
    private val resourceProvider: ResourcesProvider,
    private val saveAccessSettings: SaveAccessSettingsUseCase,
    private val mode: TemplateSettingsMode
) : TemplateInfoViewModel(
    contentResolver,
    roomProvider,
    resourceProvider,
    mode
) {

    fun deleteLogo() {
        super.setLogoUri(null)
        if (logoState.value.logoWebUrl?.isNotEmpty() == true) {
            updateLogoState { it.copy(logoWebUrl = null) }
        }
    }

    fun addOrRemoveTag(tag: String) {
        with(uiState.value) {
            val updatedTags = if (tag in tags) {
                tags - tag
            } else {
                tags + tag
            }
            updateUiState { it.copy(tags = updatedTags) }
        }
    }

    override fun setLogoUri(uri: Uri?) {
        updateUiState { it.copy(canApplyChanges = false) }
        viewModelScope.launch(Dispatchers.IO) {
            super.setLogoUri(uri)
            updateUiState { it.copy(canApplyChanges = true) }
        }
    }

    override fun save() {
        if (uiState.value.isSaving) return
        viewModelScope.launch {
            updateUiState { it.copy(isSaving = true) }
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

    fun changeTitle(title: String) {
        updateUiState { it.copy(title = title, canApplyChanges = title.isNotBlank()) }
    }

    fun updateStorageQuota(block: (StorageQuota) -> StorageQuota) {
        updateUiState { it.copy(quota = block(it.quota)) }
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