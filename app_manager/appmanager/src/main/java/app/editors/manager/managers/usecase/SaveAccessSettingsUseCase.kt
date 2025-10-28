package app.editors.manager.managers.usecase

import app.documents.core.network.common.NetworkResult
import app.documents.core.providers.RoomProvider
import app.editors.manager.viewModels.main.TemplateAccessSettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SaveAccessSettingsUseCase @Inject constructor(
    private val roomProvider: RoomProvider
) {
    suspend operator fun invoke(
        templateId: String,
        settings: TemplateAccessSettings,
        onError: (Throwable) -> Unit,
        onSuccess: suspend () -> Unit,
    ) {
        val publicResult = roomProvider.updateTemplatePublic(templateId, settings.public).first()
        if (publicResult is NetworkResult.Error) {
            onError(publicResult.exception)
            return
        } else if (settings.public) {
            onSuccess()
            return
        }

        roomProvider.updateTemplateUserAccess(
            templateId = templateId,
            users = settings.selectedUsers + settings.selectedGroups
        ).collect { accessResult ->
            when (accessResult) {
                is NetworkResult.Error -> onError(accessResult.exception)
                is NetworkResult.Success -> onSuccess()
                is NetworkResult.Loading -> Unit
            }
        }
    }
}