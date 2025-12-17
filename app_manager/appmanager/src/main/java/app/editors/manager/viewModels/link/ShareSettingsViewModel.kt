package app.editors.manager.viewModels.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.ShareType
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.managers.tools.ShareData
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
    data class Success(val links: List<ExternalLink>, val members: List<Share>) : ShareSettingsState()
}

sealed class ShareSettingsEffect {

    data class OnCreate(val loading: Boolean) : ShareSettingsEffect()
    data class Copy(val link: String) : ShareSettingsEffect()
    data class Error(val code: Int? = null) : ShareSettingsEffect()
    data object Access : ShareSettingsEffect()
}

class ShareSettingsViewModel(
    val roomProvider: RoomProvider,
    val shareData: ShareData,
) : ViewModel() {

    private val _state: MutableStateFlow<ShareSettingsState> =
        MutableStateFlow(ShareSettingsState.Loading)
    val state: StateFlow<ShareSettingsState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<ShareSettingsEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<ShareSettingsEffect> = _effect.asSharedFlow()

    val isShared: Boolean?
        get() = (state.value as? ShareSettingsState.Success)?.run {
            links.isNotEmpty() || shareData.roomType == null
                    && members.any { it.itemAccessType != ShareType.Owner }
        }

    fun create() {
        viewModelScope.launch {
            try {
                _effect.emit(ShareSettingsEffect.OnCreate(true))
                val access = if (shareData.isForm) Access.Editor else Access.Read
                val link = roomProvider.createSharedLink(
                    itemId = shareData.itemId,
                    isFolder = shareData.isFolder,
                    access = access.code
                )
                _effect.emit(ShareSettingsEffect.OnCreate(false))
                _effect.emit(ShareSettingsEffect.Copy(link.sharedTo.shareLink))

                val state = state.value
                if (state is ShareSettingsState.Success) {
                    _state.update { state.copy(links = state.links + link) }
                }
            } catch (e: HttpException) {
                val errorMessage = e.response()?.errorBody()?.string().orEmpty()
                if (ExternalLinkSettingsViewModel.MAXIMUM_LINKS_ERROR in errorMessage) {
                    _effect.emit(ShareSettingsEffect.Error(R.string.rooms_info_create_maximum_exceed))
                } else {
                    _effect.emit(ShareSettingsEffect.Error(e.code()))
                }
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
                val links = async { roomProvider.getSharedLinks(shareData.itemId, shareData.isFolder) }
                val users = async { roomProvider.getSharedUsers(shareData.itemId, shareData.isFolder) }
                _state.value = ShareSettingsState.Success(links.await(), users.await())
            } catch (e: HttpException) {
                _effect.emit(ShareSettingsEffect.Error(e.code()))
            } catch (_: Exception) {
                _effect.emit(ShareSettingsEffect.Error())
            }
        }
    }

    fun setUserAccess(userId: String, access: Access) {
        viewModelScope.launch {
            try {
                roomProvider.setItemShare(shareData.itemId, shareData.isFolder, mapOf(userId to access))
                _effect.emit(ShareSettingsEffect.Access)
            } catch (e: HttpException) {
                _effect.emit(ShareSettingsEffect.Error(e.code()))
            } catch (_: Exception) {
                _effect.emit(ShareSettingsEffect.Error())
            }
        }
    }
}