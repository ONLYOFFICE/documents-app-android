package app.editors.manager.viewModels.link

import androidx.lifecycle.viewModelScope
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
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
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ExternalLinkSettingsState(val link: ExternalLinkSharedTo, val viewStateChanged: Boolean)

sealed class ExternalLinkSettingsEffect {
    data class Error(val message: Int) : ExternalLinkSettingsEffect()
    data object Delete : ExternalLinkSettingsEffect()
    data object Save : ExternalLinkSettingsEffect()
    data object Loading : ExternalLinkSettingsEffect()
}

class ExternalLinkSettingsViewModel(
    inputLink: ExternalLinkSharedTo,
    private val roomId: String?,
    private val roomProvider: RoomProvider
) : BaseViewModel() {

    private val _state = MutableStateFlow(ExternalLinkSettingsState(inputLink, false))
    val state: StateFlow<ExternalLinkSettingsState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<ExternalLinkSettingsEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<ExternalLinkSettingsEffect> = _effect.asSharedFlow()

    private var operationJob: Job? = null

    fun deleteOrRevoke() {
        operationJob = viewModelScope.launch {
            updateLink(deleteOrRevoke = true)
            _effect.tryEmit(ExternalLinkSettingsEffect.Delete)
        }
    }

    fun save() {
        _effect.tryEmit(ExternalLinkSettingsEffect.Loading)
        operationJob = viewModelScope.launch {
            updateLink()
            _effect.tryEmit(ExternalLinkSettingsEffect.Save)
        }
    }

    fun cancelJob() {
        operationJob?.cancel()
    }

    fun updateViewState(body: ExternalLinkSharedTo.() -> ExternalLinkSharedTo) {
        val updated = body.invoke(state.value.link)
        if (updated != state.value.link) {
            _state.value = ExternalLinkSettingsState(updated, true)
        }
    }

    fun createLink() {
        _effect.tryEmit(ExternalLinkSettingsEffect.Loading)
        operationJob = viewModelScope.launch {
            try {
                with(state.value.link) {
                    roomProvider.createRoomSharedLink(
                        roomId = roomId.orEmpty(),
                        denyDownload = denyDownload,
                        expirationDate = expirationDate,
                        password = password,
                        title = title
                    )
                    _effect.tryEmit(ExternalLinkSettingsEffect.Save)
                }
            } catch (httpException: HttpException) {
                onError(httpException)
            }
        }
    }

    private suspend fun updateLink(deleteOrRevoke: Boolean = false): ExternalLink? {
        _effect.tryEmit(ExternalLinkSettingsEffect.Loading)
        return try {
            with(state.value.link) {
                roomProvider.updateRoomSharedLink(
                    roomId = roomId.orEmpty(),
                    access = if (!deleteOrRevoke) 2 else 0,
                    linkId = id,
                    linkType = linkType,
                    denyDownload = denyDownload,
                    expirationDate = expirationDate,
                    password = password,
                    title = title
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