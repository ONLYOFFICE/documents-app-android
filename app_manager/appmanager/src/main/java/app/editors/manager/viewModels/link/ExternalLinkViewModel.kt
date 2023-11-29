package app.editors.manager.viewModels.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.Share
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ExternalLinkState(
    val generalLink: ExternalLink? = null,
    val additionalLinks: List<ExternalLink> = emptyList(),
    val shareList: List<Share> = emptyList()
)

sealed class ExternalLinkEffect {

    data class Error(val message: String) : ExternalLinkEffect()
}

class ExternalLinkViewModelFactory(private val roomProvider: RoomProvider) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExternalLinkViewModel(roomProvider) as T
    }

}

class ExternalLinkViewModel(private val roomProvider: RoomProvider) : ViewModel() {

    private val _state: MutableStateFlow<ExternalLinkState> = MutableStateFlow(ExternalLinkState())
    val state: StateFlow<ExternalLinkState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<ExternalLinkEffect> = MutableSharedFlow()
    val effect: SharedFlow<ExternalLinkEffect> = _effect.asSharedFlow()

    fun fetchRoomInfo(roomId: String) {
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
                _state.value = ExternalLinkState(
                    generalLink = generalLink,
                    additionalLinks = additionalLinks,
                    shareList = roomProvider.getRoomUsers(roomId)
                )
            } catch (httpException: HttpException) {
                _effect.emit(
                    ExternalLinkEffect.Error(
                        httpException.response()?.errorBody()?.string()
                            ?: httpException.message
                            ?: "some error"
                    )
                )
            }
        }
    }

}