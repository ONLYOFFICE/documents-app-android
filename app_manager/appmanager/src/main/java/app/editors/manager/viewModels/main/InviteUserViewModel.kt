package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.network.manager.models.explorer.AccessTarget
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.providers.RoomProvider
import app.editors.manager.managers.tools.ShareData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InviteUserState(
    val screenLoading: Boolean = false,
    val requestLoading: Boolean = false,
    val externalLink: ExternalLink?,
)

class InviteUserViewModel(
    private val roomId: String,
    private val roomType: Int,
    private val roomProvider: RoomProvider
) : ViewModel() {

    private val _state: MutableStateFlow<InviteUserState> = MutableStateFlow(
        InviteUserState(
            screenLoading = true,
            externalLink = null
        )
    )

    val state: StateFlow<InviteUserState> = _state.asStateFlow()

    private val _error: MutableSharedFlow<Exception> = MutableSharedFlow(1)
    val error: SharedFlow<Exception> = _error.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.value = InviteUserState(externalLink = roomProvider.getRoomInviteLink(roomId))
        }
    }

    fun setInviteLinkEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(requestLoading = true) }
            try {
                if (enabled) {
                    val access = ShareData(roomType = roomType)
                        .getAccessList(AccessTarget.ExternalLink)
                        .last()
                    val link = roomProvider.addRoomInviteLink(roomId, access.access.code)
                    _state.value = InviteUserState(externalLink = link)
                } else {
                    roomProvider.removeRoomInviteLink(
                        roomId = roomId,
                        linkId = state.value.externalLink?.sharedTo?.id.orEmpty()
                    )
                    _state.value = InviteUserState(externalLink = null)
                }
            } catch (e: Exception) {
                _error.emit(e)
            } finally {
                _state.update { it.copy(requestLoading = false) }
            }
        }
    }

    fun setAccess(access: Access) {
        viewModelScope.launch {
            _state.update { it.copy(requestLoading = true) }
            try {
                val link = roomProvider.setRoomInviteLinkAccess(
                    roomId = roomId,
                    linkId = state.value.externalLink?.sharedTo?.id.orEmpty(),
                    access = access.code
                )
                _state.value = InviteUserState(externalLink = link)
            } catch (e: Exception) {
                _error.emit(e)
            } finally {
                _state.update { it.copy(requestLoading = false) }
            }
        }
    }
}