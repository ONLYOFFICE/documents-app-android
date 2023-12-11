package app.editors.manager.viewModels.link

import androidx.lifecycle.viewModelScope
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.viewModels.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ExternalLinkSettingsState(val link: ExternalLink, val viewStateChanged: Boolean)

sealed class ExternalLinkSettingsEffect {
    data class Share(val url: String) : ExternalLinkSettingsEffect()
    data class Copy(val url: String) : ExternalLinkSettingsEffect()
    data class Error(val message: Int) : ExternalLinkSettingsEffect()
    data object Save : ExternalLinkSettingsEffect()
    data object Loading : ExternalLinkSettingsEffect()
}

class ExternalLinkSettingsViewModel(
    inputLink: ExternalLink,
    private val roomId: String?,
    private val roomProvider: RoomProvider
) : BaseViewModel() {

    private val _state = MutableStateFlow(ExternalLinkSettingsState(inputLink, false))
    val state: StateFlow<ExternalLinkSettingsState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<ExternalLinkSettingsEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<ExternalLinkSettingsEffect> = _effect.asSharedFlow()

    private var operationJob: Job? = null

    fun delete() {

    }

    fun save() {
        operationJob = viewModelScope.launch {
            updateExternalLink()
            _effect.tryEmit(ExternalLinkSettingsEffect.Save)
        }
    }

    fun share() {
        operationJob = viewModelScope.launch {
            val url = updateExternalLink()?.sharedTo?.shareLink
            if (url != null) {
                _effect.tryEmit(ExternalLinkSettingsEffect.Share(url))
                _state.update { it.copy(viewStateChanged = false) }
            } else {
                onError(null)
            }
        }
    }

    fun copy() {
        operationJob = viewModelScope.launch {
            val url = updateExternalLink()?.sharedTo?.shareLink
            if (url != null) {
                _effect.tryEmit(ExternalLinkSettingsEffect.Copy(url))
                _state.update { it.copy(viewStateChanged = false) }
            } else {
                onError(null)
            }
        }
    }

    fun cancelJob() {
        operationJob?.cancel()
    }

    fun updateViewState(body: ExternalLink.() -> ExternalLink) {
        val initial = state.value.link
        val updated = body.invoke(state.value.link)
        if (updated.sharedTo != initial.sharedTo) {
            _state.value = ExternalLinkSettingsState(initial.copy(sharedTo = updated.sharedTo), true)
        }
    }

    private suspend fun updateExternalLink(): ExternalLink? {
        _effect.tryEmit(ExternalLinkSettingsEffect.Loading)
        return try {
            with(state.value.link) {
                roomProvider.updateExternalLink(
                    roomId = roomId.orEmpty(),
                    access = access,
                    linkId = sharedTo.id,
                    linkType = sharedTo.linkType,
                    denyDownload = sharedTo.denyDownload,
                    expirationDate = sharedTo.expirationDate,
                    password = sharedTo.password,
                    title = sharedTo.title
                )
            }
        } catch (httpException: HttpException) {
            onError(httpException)
            null
        }
    }


    private fun onError(httpException: HttpException?) {
        viewModelScope.launch {
            val message = when (httpException?.code()) {
                ApiContract.HttpCodes.CLIENT_FORBIDDEN -> R.string.errors_client_forbidden
                else -> R.string.errors_unknown_error
            }
            _effect.tryEmit(ExternalLinkSettingsEffect.Error(message))
        }
    }

}