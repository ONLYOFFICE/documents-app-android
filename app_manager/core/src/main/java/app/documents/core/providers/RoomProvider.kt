package app.documents.core.providers

import android.graphics.Bitmap
import androidx.core.text.isDigitsOnly
import app.documents.core.network.common.Result
import app.documents.core.network.common.asResult
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Operation
import app.documents.core.network.manager.models.request.RequestBatchOperation
import app.documents.core.network.manager.models.request.RequestRoomNotifications
import app.documents.core.network.room.RoomService
import app.documents.core.network.room.models.RequestAddTags
import app.documents.core.network.room.models.RequestArchive
import app.documents.core.network.room.models.RequestCreateExternalLink
import app.documents.core.network.room.models.RequestCreateRoom
import app.documents.core.network.room.models.RequestCreateTag
import app.documents.core.network.room.models.RequestDeleteRoom
import app.documents.core.network.room.models.RequestRenameRoom
import app.documents.core.network.room.models.RequestRoomOwner
import app.documents.core.network.room.models.RequestSetLogo
import app.documents.core.network.room.models.RequestUpdateExternalLink
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.GroupShare
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.request.EmailInvitation
import app.documents.core.network.share.models.request.Invitation
import app.documents.core.network.share.models.request.RequestAddInviteLink
import app.documents.core.network.share.models.request.RequestCreateSharedLink
import app.documents.core.network.share.models.request.RequestCreateThirdPartyRoom
import app.documents.core.network.share.models.request.RequestRemoveInviteLink
import app.documents.core.network.share.models.request.RequestRoomShare
import app.documents.core.network.share.models.request.RequestUpdateSharedLink
import app.documents.core.network.share.models.request.UserIdInvitation
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.FileUtils.toByteArray
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject

class RoomProvider @Inject constructor(private val roomService: RoomService) {

    suspend fun archiveRoom(id: String, isArchive: Boolean) {
        if (isArchive) {
            require(roomService.archive(id, RequestArchive()).isSuccessful)
        } else {
            require(roomService.unarchive(id, RequestArchive()).isSuccessful)
        }
    }

    fun pinRoom(id: String, isPin: Boolean = true): Observable<BaseResponse> {
        return if (isPin) {
            roomService.pinRoom(id)
        } else {
            roomService.unpinRoom(id)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { if (it.isSuccessful) Observable.just(it.body()) else throw HttpException(it) }
    }

    suspend fun renameRoom(id: String, newTitle: String): Boolean {
        return roomService.renameRoom(id, RequestRenameRoom(title = newTitle)).isSuccessful
    }

    suspend fun createRoom(title: String, type: Int): String {
        val response = roomService.createRoom(
            RequestCreateRoom(
                title = title,
                roomType = type
            )
        )
        return response.body()?.response?.id ?: ""
    }

    suspend fun createThirdPartyRoom(folderId: String, title: String, asNewFolder: Boolean): String {
        val response = roomService.createThirdPartyRoom(
            folderId,
            RequestCreateThirdPartyRoom(
                title = title,
                roomType = ApiContract.RoomType.PUBLIC_ROOM,
                createAsNewFolder = asNewFolder
            )
        )
        return response.body()?.response?.id ?: ""
    }

    fun deleteRoom(id: String = "", items: List<String>? = null): Observable<BaseResponse> {
        return if (items != null && id.isEmpty()) {
            Observable.fromIterable(items)
                .subscribeOn(Schedulers.io())
                .flatMap { itemId -> roomService.deleteRoom(itemId, RequestDeleteRoom()) }
                .map { it.body() }
                .lastElement()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable { Observable.just(it) }
        } else if (id.isNotEmpty()) {
            roomService.deleteRoom(id, RequestDeleteRoom())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        } else {
            Observable.empty()
        }
    }

    suspend fun addTags(id: String, tags: List<String>): Boolean {
        val existTags = roomService.getTags().tags
        withContext(Dispatchers.IO) {
            tags.forEach { newTag ->
                if (!existTags.contains(newTag)) {
                    launch { roomService.createTag(RequestCreateTag(newTag)) }
                }
            }
        }
        return roomService.addTags(id, RequestAddTags(tags.toTypedArray())).isSuccessful
    }

    suspend fun deleteTags(id: String, tag: List<String>): Boolean {
        return roomService.deleteTagsFromRoom(id, RequestAddTags(tag.toTypedArray())).isSuccessful
    }

    suspend fun deleteLogo(id: String): Boolean {
        return roomService.deleteLogo(id).isSuccessful
    }

    suspend fun getRoomInviteLink(id: String): ExternalLink? {
        return roomService.setRoomInviteLink(id).response?.getOrNull(0)
    }

    suspend fun addRoomInviteLink(roomId: String, access: Int): ExternalLink {
        return roomService.addRoomInviteLink(roomId, RequestAddInviteLink(access = access)).response
    }

    suspend fun removeRoomInviteLink(roomId: String, linkId: String) {
        roomService.removeRoomInviteLink(roomId, RequestRemoveInviteLink(linkId = linkId))
    }

    suspend fun setRoomInviteLinkAccess(roomId: String, linkId: String, access: Int): ExternalLink? {
        return roomService.removeRoomInviteLink(
            roomId,
            RequestRemoveInviteLink(access = access, linkId = linkId)
        ).response
    }

    suspend fun getRoomSharedLinks(id: String): List<ExternalLink> {
        val response = roomService.getRoomSharedLinks(id)
        val body = response.body()
        return if (response.isSuccessful && body != null) body.response else throw HttpException(response)
    }

    suspend fun updateRoomSharedLink(
        roomId: String?,
        access: Int?,
        linkId: String?,
        linkType: Int?,
        denyDownload: Boolean?,
        expirationDate: String?,
        password: String?,
        title: String?,
    ): ExternalLink {
        val request = RequestUpdateExternalLink(
            access = access ?: ApiContract.ShareCode.READ,
            denyDownload = denyDownload == true,
            expirationDate = expirationDate,
            linkId = linkId,
            linkType = linkType ?: 2,
            password = password,
            title = title
        )
        val response = roomService.updateRoomSharedLink(roomId.orEmpty(), request)
        val body = response.body()
        return if (response.isSuccessful && body != null)
            body.response else throw HttpException(response)
    }

    suspend fun createRoomSharedLink(
        roomId: String?,
        denyDownload: Boolean,
        expirationDate: String?,
        password: String?,
        title: String,
    ): ExternalLink {
        val request = RequestCreateExternalLink(
            denyDownload = denyDownload,
            expirationDate = expirationDate,
            password = password,
            title = title
        )
        val response = roomService.createRoomSharedLink(roomId.orEmpty(), request)
        val body = response.body()
        return if (response.isSuccessful && body != null) body.response else throw HttpException(response)
    }

    suspend fun getSharedLinks(id: String): List<ExternalLink> {
        val response = roomService.getSharedLinks(id)
        val body = response.body()
        return if (response.isSuccessful && body != null) body.response else throw HttpException(response)
    }

    suspend fun createSharedLink(fileId: String): ExternalLink {
        return roomService.createSharedLink(fileId, RequestCreateSharedLink()).response
    }

    suspend fun updateSharedLink(fileId: String, sharedLink: ExternalLink): ExternalLink {
        return roomService.updateSharedLink(fileId, RequestUpdateSharedLink.from(sharedLink)).response
    }

    suspend fun getRoomUsers(id: String): List<Share> {
        val response = roomService.getRoomUsers(id)
        val body = response.body()
        return if (response.isSuccessful && body != null) body.response else throw HttpException(response)
    }

    suspend fun setRoomUserAccess(roomId: String, userId: String, access: Int): Share? {
        val body = RequestRoomShare(listOf(Invitation(id = userId, access = access)))
        val response = roomService.setRoomUserAccess(roomId, body)
        return if (response.isSuccessful)
            response.body()?.response?.members?.getOrNull(0) else throw HttpException(response)
    }

    suspend fun setLogo(id: String, logo: Bitmap) {
        val logoId = UUID.randomUUID().toString()
        val uploadResponse = roomService.uploadLogo(
            MultipartBody.Part.createFormData(
                logoId,
                "$logoId.png",
                RequestBody.create(MediaType.get("image/*"), logo.toByteArray())
            )
        )
        if (uploadResponse.isSuccessful) {
            roomService.setLogo(
                id,
                RequestSetLogo(
                    tmpFile = JSONObject(uploadResponse.body()?.string() ?: "").optJSONObject("response")
                        ?.optString("data")
                        ?: "",
                    width = logo.width,
                    height = logo.height
                )
            )
        }
    }

    suspend fun setRoomOwner(id: String, userId: String): CloudFolder {
        val result = roomService.setOwner(RequestRoomOwner(userId, listOf(id)))
        return result.response[0]
    }

    suspend fun leaveRoom(roomId: String, userId: String) {
        inviteById(roomId, mapOf(userId to ApiContract.ShareCode.NONE))
    }

    suspend fun inviteByEmail(roomId: String, emails: Map<String, Int>) {
        val response = roomService.shareRoom(
            id = roomId,
            body = RequestRoomShare(
                invitations = emails.map { (email, access) -> EmailInvitation(email = email, access = access) },
                notify = false
            )
        )
        if (!response.isSuccessful) throw HttpException(response)
    }

    suspend fun inviteById(roomId: String, users: Map<String, Int>) {
        val response = roomService.shareRoom(
            id = roomId,
            body = RequestRoomShare(
                invitations = users.map { (id, access) -> UserIdInvitation(id, access) },
                notify = false
            )
        )
        if (!response.isSuccessful) throw HttpException(response)
    }

    suspend fun getGroupUsers(roomId: String, groupId: String): List<GroupShare> {
        return roomService.getGroupUsers(roomId, groupId).response
    }

    suspend fun getExternalLink(roomId: String): String {
        return roomService.getExternalLink(roomId).response.sharedTo.shareLink
    }

    suspend fun copyItems(roomId: String, folderIds: List<String>, fileIds: List<String>) {
        val request = RequestBatchOperation(destFolderId = roomId).apply {
            this.folderIds = folderIds
            this.fileIds = fileIds
        }

        roomService.copy(request)
            .response
            .forEach { operation -> waitOperationIsFinished(operation) }
    }

    private suspend fun waitOperationIsFinished(operation: Operation) {
        while (true) {
            val status = roomService.status()
                .response
                .find { it.id == operation.id } ?: break

            if (status.progress == 100 || status.finished || !status.error.isNullOrEmpty()) break
            delay(1000)
        }
    }

    suspend fun duplicate(roomId: String): Flow<Result<Int>> {
        return flow {
            val response = roomService.duplicate(RequestBatchOperation().apply { folderIds = listOf(roomId) })
            for (operation in response.response) {
                if (operation.finished) continue
                while (true) {
                    val status = roomService.status().response.find { it.id == operation.id }
                    if (status == null || status.finished || status.progress >= 100) break
                    emit(status.progress)
                    delay(1000)
                }
            }
            emit(100)
        }.asResult()
    }

    fun muteRoomNotifications(roomId: String, muted: Boolean): Flow<Result<List<String>>> {
        return flow<List<String>> {
            if (!roomId.isDigitsOnly()) throw IllegalArgumentException()
            val rooms = roomService.muteNotifications(
                RequestRoomNotifications(
                    roomsId = roomId.toInt(),
                    mute = muted
                )
            ).response.disabledRooms
            emit(rooms)
        }
            .flowOn(Dispatchers.IO)
            .asResult()
    }
}