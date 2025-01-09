package app.editors.manager.viewModels.link

import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.Share
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.viewModels.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class RoomInfoState(
    val isLoading: Boolean = true,
    val sharedLinks: List<ExternalLink> = emptyList(),
    val shareList: List<Share> = emptyList()
)

sealed class RoomInfoEffect {

    data object ShowOperationDialog : RoomInfoEffect()
    data object CloseDialog : RoomInfoEffect()
    data class Error(val message: Int) : RoomInfoEffect()
    data class Create(val url: String) : RoomInfoEffect()
}

class RoomInfoViewModel(private val roomProvider: RoomProvider, private val roomId: String) : BaseViewModel() {

    private val _state: MutableStateFlow<RoomInfoState> = MutableStateFlow(RoomInfoState())
    val state: StateFlow<RoomInfoState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<RoomInfoEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<RoomInfoEffect> = _effect.asSharedFlow()

    private var operationJob: Job? = null

    fun fetchRoomInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.value = RoomInfoState(
                    isLoading = false,
                    sharedLinks = roomProvider.getRoomSharedLinks(roomId),
                    shareList = roomProvider.getRoomUsers(roomId)
                )
            } catch (httpException: HttpException) {
                onError(httpException)
            }
        }
    }

    fun setUserAccess(roomId: String, userId: String, access: Access) {
        _effect.tryEmit(RoomInfoEffect.ShowOperationDialog)
        operationJob = viewModelScope.launch {
            try {
                roomProvider.setRoomUserAccess(roomId, userId, access.code)
                fetchRoomInfo()
                _effect.tryEmit(RoomInfoEffect.CloseDialog)
            } catch (httpException: HttpException) {
                onError(httpException)
            }
        }
    }

    fun cancelOperation() {
        operationJob?.cancel()
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