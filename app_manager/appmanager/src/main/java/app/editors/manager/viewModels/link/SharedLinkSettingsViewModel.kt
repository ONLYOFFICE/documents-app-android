package app.editors.manager.viewModels.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class SharedLinkSettingsEffect {

    data object Close : SharedLinkSettingsEffect()
    data object Delete : SharedLinkSettingsEffect()
    data class Error(val code: Int? = null) : SharedLinkSettingsEffect()
}

class SharedLinkSettingsViewModel(
    externalLink: ExternalLink,
    private val roomProvider: RoomProvider,
    private val fileId: String
) : ViewModel() {

    private val _state: MutableStateFlow<ExternalLink> = MutableStateFlow(externalLink)
    val state: StateFlow<ExternalLink> = _state.asStateFlow()

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _effect: MutableSharedFlow<SharedLinkSettingsEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<SharedLinkSettingsEffect> = _effect.asSharedFlow()

    fun setInternal(internal: Boolean) {
        tryRequest {
            if (state.value.sharedTo.internal != internal) {
                val link = state.value.copy(sharedTo = state.value.sharedTo.copy(internal = internal))
                roomProvider.updateSharedLink(fileId, link)
                _state.value = link
            }
        }
    }

    fun regenerate() {

    }

    fun delete() {
        tryRequest {
            val link = state.value.copy(access = 0)
            roomProvider.updateSharedLink(fileId, link)
            _effect.tryEmit(SharedLinkSettingsEffect.Delete)
        }
    }

    fun setAccess(access: Int) {
        tryRequest {
            val link = state.value.copy(access = access)
            roomProvider.updateSharedLink(fileId, link)
            _state.value = link
        }
    }

    private fun tryRequest(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _loading.value = true
                block.invoke()
            } catch (e: HttpException) {
                _effect.tryEmit(SharedLinkSettingsEffect.Error(e.code()))
            } catch (e: Exception) {
                _effect.tryEmit(SharedLinkSettingsEffect.Error())
            } finally {
                _loading.value = false
            }
        }
    }
}