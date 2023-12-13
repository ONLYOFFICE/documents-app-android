package app.editors.manager.viewModels.link

import androidx.lifecycle.viewModelScope
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.Share
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.viewModels.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class RoomInfoState(
    val generalLink: ExternalLink? = null,
    val additionalLinks: List<ExternalLink> = emptyList(),
    val shareList: List<Share> = emptyList()
)

sealed class RoomInfoEffect {

    data class Error(val message: Int) : RoomInfoEffect()
}

class RoomInfoViewModel(private val roomProvider: RoomProvider, private val roomId: String) : BaseViewModel() {

    private val _state: MutableStateFlow<RoomInfoState> = MutableStateFlow(RoomInfoState())
    val state: StateFlow<RoomInfoState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<RoomInfoEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<RoomInfoEffect> = _effect.asSharedFlow()

    fun fetchRoomInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var generalLink: ExternalLink? = null
                val additionalLinks = mutableListOf<ExternalLink>()
                roomProvider.getExternalLinks(roomId).forEach { link ->
                    if (link.sharedTo.primary) {
                        generalLink = link
                    } else {
                        additionalLinks.add(link)
                    }
                }
                _state.value = RoomInfoState(
                    generalLink = generalLink,
                    additionalLinks = additionalLinks,
                    shareList = roomProvider.getRoomUsers(roomId)
                )
            } catch (httpException: HttpException) {
                onError(httpException)
            }
        }
    }

    fun setUserAccess(roomId: String, userId: String, access: Int) {
        viewModelScope.launch {
            try {
                val share = roomProvider.setRoomUserAccess(roomId, userId, access)
                if (share != null) {
                    val shareList = _state.value.shareList
                    val index = shareList.indexOfFirst { it.sharedTo.id == userId }
                    val modifiedList = shareList.toMutableList()

                    if (access == ApiContract.ShareCode.NONE) {
                        modifiedList.removeAt(index)
                    } else {
                        modifiedList[index] = shareList[index].copy(access = share.access)
                    }
                    _state.value = _state.value.copy(shareList = modifiedList)
                } else {
                    fetchRoomInfo()
                }
            } catch (httpException: HttpException) {
                onError(httpException)
            }
        }
    }

    fun createGeneralLink() {
        viewModelScope.launch {
            try {
                roomProvider.createGeneralLink(roomId)
                fetchRoomInfo()
            } catch (httpException: HttpException) {
                onError(httpException)
            }
        }
    }

    private fun onError(httpException: HttpException) {
        viewModelScope.launch {
            val message = when (httpException.code()) {
                ApiContract.HttpCodes.CLIENT_FORBIDDEN -> R.string.errors_client_forbidden
                else -> R.string.errors_unknown_error
            }
            _effect.tryEmit(RoomInfoEffect.Error(message))
        }
    }

}