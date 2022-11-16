package app.documents.core.network.share

import app.documents.core.network.common.models.Base
import app.documents.core.network.share.models.request.*
import app.documents.core.network.share.models.response.ResponseExternal
import app.documents.core.network.share.models.response.ResponseGroups
import app.documents.core.network.share.models.response.ResponseShare
import app.documents.core.network.share.models.response.ResponseUsers
import app.documents.core.repositories.ShareRepository

internal class ShareRepositoryImpl(private val shareService: ShareService) : ShareRepository {

    override suspend fun getShareFolder(folderId: String): ResponseShare {
        return shareService.getShareFolder(folderId)
    }

    override suspend fun getShareFile(fileId: String): ResponseShare {
        return shareService.getShareFile(fileId)
    }

    override suspend fun getExternalLink(fileId: String, body: RequestExternal): ResponseExternal {
        return shareService.getExternalLink(fileId, body)
    }

    override suspend fun setExternalLinkAccess(fileId: String, body: RequestExternalAccess): Base {
        return shareService.setExternalLinkAccess(fileId, body)
    }

    override suspend fun setFolderAccess(folderId: String, body: RequestShare): ResponseShare {
        return shareService.setFolderAccess(folderId, body)
    }

    override suspend fun setFileAccess(fileId: String, body: RequestShare): ResponseShare {
        return shareService.setFileAccess(fileId, body)
    }

    override suspend fun deleteShare(token: String, body: RequestDeleteShare): Base {
        return shareService.deleteShare(token, body)
    }

    override suspend fun getGroups(options: Map<String, String>): ResponseGroups {
        return shareService.getGroups(options)
    }

    override suspend fun getUsers(options: Map<String, String>): ResponseUsers {
        return shareService.getUsers(options)
    }

    override suspend fun shareRoom(roomId: String, body: RequestRoomShare): ResponseShare {
        return shareService.shareRoom(roomId, body)
    }

}