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

sealed class ShareSettingsState {

    data object Loading : ShareSettingsState()
    data class Success(val links: List<ExternalLink>) : ShareSettingsState()
}

sealed class ShareSettingsEffect {

    data class OnCreate(val loading: Boolean) : ShareSettingsEffect()
    data class Copy(val link: String) : ShareSettingsEffect()
    data class Error(val code: Int? = null) : ShareSettingsEffect()
}

class ShareSettingsViewModel(
    val roomProvider: RoomProvider,
    val itemId: String,
    val isFolder: Boolean
) : ViewModel() {

    private val _state: MutableStateFlow<ShareSettingsState> =
        MutableStateFlow(ShareSettingsState.Loading)
    val state: StateFlow<ShareSettingsState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<ShareSettingsEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<ShareSettingsEffect> = _effect.asSharedFlow()

    fun create() {
        viewModelScope.launch {
            try {
                _effect.emit(ShareSettingsEffect.OnCreate(true))
                val link = roomProvider.createSharedLink(itemId, isFolder)
                _effect.emit(ShareSettingsEffect.OnCreate(false))
                _effect.emit(ShareSettingsEffect.Copy(link.sharedTo.shareLink))

                val state = state.value
                if (state is ShareSettingsState.Success) {
                    _state.value = ShareSettingsState.Success(state.links + link)
                }
            } catch (e: HttpException) {
                _effect.emit(ShareSettingsEffect.Error(e.code()))
            } catch (_: Exception) {
                _effect.emit(ShareSettingsEffect.Error())
            } finally {
                _effect.emit(ShareSettingsEffect.OnCreate(false))
            }
        }
    }

    fun fetchLinks() {
        viewModelScope.launch {
            try {
                val links = roomProvider.getSharedLinks(itemId, isFolder)
                _state.value = ShareSettingsState.Success(links)
            } catch (e: HttpException) {
                _effect.emit(ShareSettingsEffect.Error(e.code()))
            } catch (_: Exception) {
                _effect.emit(ShareSettingsEffect.Error())
            }
        }
    }
}