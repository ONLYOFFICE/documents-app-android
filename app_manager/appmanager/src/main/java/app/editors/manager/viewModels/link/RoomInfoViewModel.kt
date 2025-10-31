package app.editors.manager.viewModels.link

import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.Share
import app.documents.core.providers.RoomProvider

data class SharingState(
    val isLoading: Boolean = true,
    val isCreateLoading: Boolean = false,
    val requestLoading: Boolean = false,
    val sharedLinks: List<ExternalLink> = emptyList(),
    val shareList: List<Share> = emptyList(),
    val canAddLinks: Boolean = sharedLinks.size < 6
)

sealed class SharingEffect {
    data class Error(val message: Int) : SharingEffect()
    data class Create(val url: String) : SharingEffect()
}

class RoomInfoViewModel(
    private val roomProvider: RoomProvider,
    private val roomId: String
) : BaseSharingViewModel() {

    override suspend fun setUserAccess(
        itemId: String,
        userId: String,
        access: Int
    ): Share? = roomProvider.setRoomUserAccess(roomId, userId, access)

    override suspend fun getSharedLinks(): List<ExternalLink> =
        roomProvider.getRoomSharedLinks(roomId)

    override suspend fun getUsers(): List<Share> = roomProvider.getRoomUsers(roomId)
    override suspend fun createExternalLink() = null
}