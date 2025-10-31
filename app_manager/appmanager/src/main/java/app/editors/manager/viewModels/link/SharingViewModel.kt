package app.editors.manager.viewModels.link

import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.Share
import app.documents.core.providers.RoomProvider
import app.editors.manager.ui.fragments.share.link.ShareItemType

class SharingViewModel(
    private val roomProvider: RoomProvider,
    private val itemId: String,
    private val shareType: ShareItemType
) : BaseSharingViewModel() {

    override suspend fun setUserAccess(
        itemId: String,
        userId: String,
        access: Int
    ): Share? {
        return if (shareType.isFolder) {
            roomProvider.setShareUserAccess(
                id = itemId,
                userId = userId,
                access = access.toString(),
                isFolder = true
            )
        } else {
            null
        }
    }

    override suspend fun getSharedLinks(): List<ExternalLink> =
        roomProvider.getSharedLinks(itemId, shareType.isFolder)

    override suspend fun getUsers(): List<Share> {
        return if (shareType.shouldShowUsers) {
            emptyList()
        } else {
            roomProvider.getShareUsers(itemId, shareType.isFolder)
        }
    }

    override suspend fun createExternalLink(): ExternalLink? {
        return roomProvider.createSharedLink(itemId, shareType.isFolder)
    }
}