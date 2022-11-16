package app.documents.core.repositories

import app.documents.core.network.common.models.Base
import app.documents.core.network.share.models.request.*
import app.documents.core.network.share.models.response.ResponseExternal
import app.documents.core.network.share.models.response.ResponseGroups
import app.documents.core.network.share.models.response.ResponseShare
import app.documents.core.network.share.models.response.ResponseUsers

interface ShareRepository {

    suspend fun getShareFolder(folderId: String): ResponseShare

    suspend fun getShareFile(fileId: String): ResponseShare

    suspend fun getExternalLink(fileId: String, body: RequestExternal): ResponseExternal

    suspend fun setExternalLinkAccess(fileId: String, body: RequestExternalAccess): Base

    suspend fun setFolderAccess(folderId: String, body: RequestShare): ResponseShare

    suspend fun setFileAccess(fileId: String, body: RequestShare): ResponseShare

    suspend fun deleteShare(token: String, body: RequestDeleteShare): Base

    suspend fun getGroups(options: Map<String, String> = mapOf()): ResponseGroups

    suspend fun getUsers(options: Map<String, String> = mapOf()): ResponseUsers

    suspend fun shareRoom(roomId: String, body: RequestRoomShare): ResponseShare

}