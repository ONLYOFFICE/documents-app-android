package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InviteUserState(
    val screenLoading: Boolean,
    val requestLoading: Boolean,
    val externalLink: ExternalLink?
)

class InviteUserViewModel(private val roomProvider: RoomProvider) : ViewModel() {

    private val _state: MutableStateFlow<InviteUserState> = MutableStateFlow(
        InviteUserState(
            screenLoading = true,
            requestLoading = false,
            externalLink = null
        )
    )

    val state: StateFlow<InviteUserState> = _state.asStateFlow()

    fun fetchInviteLink(roomId: String) {
        viewModelScope.launch {
            _state.value = InviteUserState(
                screenLoading = false,
                requestLoading = false,
                externalLink = roomProvider.getRoomInviteLink(roomId)
            )
        }
    }

    fun setInviteLinkEnabled(roomId: String, enabled: Boolean) {
        viewModelScope.launch {
            _state.value = InviteUserState(
                screenLoading = false,
                requestLoading = false,
                externalLink = roomProvider.enableRoomExternalLink(
                    roomId,
                    enabled,
                    state.value.externalLink?.sharedTo?.id
                )
            )
        }
    }
}