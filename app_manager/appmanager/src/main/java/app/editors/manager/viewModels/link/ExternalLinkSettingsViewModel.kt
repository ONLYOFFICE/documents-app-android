package app.editors.manager.viewModels.link

import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.StringUtils
import retrofit2.HttpException

data class ExternalLinkSettingsState(
    val loading: Boolean,
    val link: ExternalLinkSharedTo,
    val access: Access
)

sealed class ExternalLinkSettingsEffect {
    data class Error(val message: Int) : ExternalLinkSettingsEffect()
    data object Delete : ExternalLinkSettingsEffect()
    data object Save : ExternalLinkSettingsEffect()
    data object PasswordLength : ExternalLinkSettingsEffect()
    data object PasswordForbiddenSymbols : ExternalLinkSettingsEffect()
}

class ExternalLinkSettingsViewModel(
    access: Access,
    inputLink: ExternalLinkSharedTo,
    private val roomId: String?,
    private val roomProvider: RoomProvider,
) : BaseViewModel() {

    companion object {

        private const val MAXIMUM_LINKS_ERROR = "The maximum number of links"
    }

    private val _state = MutableStateFlow(ExternalLinkSettingsState(false, inputLink, access))
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
        operationJob = viewModelScope.launch {
            updateLink()
            _effect.tryEmit(ExternalLinkSettingsEffect.Save)
        }
    }

    fun cancelJob() {
        operationJob?.cancel()
    }

    fun updateViewState(body: ExternalLinkSharedTo.() -> ExternalLinkSharedTo) {
        _state.update {
            it.copy(link = body(it.link))
        }
    }

    fun createLink() {
        if (!validatePassword()) return
        _state.update { it.copy(loading = true) }
        operationJob = viewModelScope.launch {
            try {
                with(state.value.link) {
                    roomProvider.createRoomSharedLink(
                        roomId = roomId.orEmpty(),
                        denyDownload = denyDownload,
                        expirationDate = expirationDate,
                        password = password,
                        title = title,
                        access = state.value.access
                    )
                    _effect.tryEmit(ExternalLinkSettingsEffect.Save)
                }
            } catch (httpException: HttpException) {
                val errorMessage = httpException.response()?.errorBody()?.string().orEmpty()
                if (MAXIMUM_LINKS_ERROR in errorMessage) {
                    _effect.tryEmit(ExternalLinkSettingsEffect.Error(R.string.rooms_info_create_maximum_exceed))
                } else {
                    onError(httpException)
                }
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun setAccess(access: Access) {
        _state.update { it.copy(access = access) }
    }

    private suspend fun updateLink(deleteOrRevoke: Boolean = false): ExternalLink? {
        if (!validatePassword()) return null
        _state.update { it.copy(loading = true) }
        return try {
            with(state.value.link) {
                roomProvider.updateRoomSharedLink(
                    roomId = roomId.orEmpty(),
                    access = if (deleteOrRevoke) Access.None else state.value.access,
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
        } finally {
            _state.update { it.copy(loading = false) }
        }
    }

    private fun validatePassword(): Boolean {
        val password = state.value.link.password ?: return true

        if (password.length < 8) {
            _effect.tryEmit(ExternalLinkSettingsEffect.PasswordLength)
            return false
        }

        if (!StringUtils.PASSWORD_FORBIDDEN_SYMBOLS.toRegex().matches(password)) {
            _effect.tryEmit(ExternalLinkSettingsEffect.PasswordForbiddenSymbols)
            return false
        }

        return true
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