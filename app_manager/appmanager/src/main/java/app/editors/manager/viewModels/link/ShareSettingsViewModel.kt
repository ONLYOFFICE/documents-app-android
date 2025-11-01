package app.editors.manager.viewModels.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.Share
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class ShareSettingsState {

    data object Loading : ShareSettingsState()
    data class Success(val links: List<ExternalLink>, val users: List<Share>) : ShareSettingsState()
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
                    _state.update { state.copy(links = state.links + link) }
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

    fun fetchData() {
        viewModelScope.launch {
            try {
                val links = async { roomProvider.getSharedLinks(itemId, isFolder) }
                val users = async { roomProvider.getSharedUsers(itemId, isFolder) }
                _state.value = ShareSettingsState.Success(links.await(), users.await())
            } catch (e: HttpException) {
                _effect.emit(ShareSettingsEffect.Error(e.code()))
            } catch (_: Exception) {
                _effect.emit(ShareSettingsEffect.Error())
            }
        }
    }
}