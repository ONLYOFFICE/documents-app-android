package app.documents.core.providers

import android.graphics.Bitmap
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.room.RoomService
import app.documents.core.network.room.models.RequestAddTags
import app.documents.core.network.room.models.RequestArchive
import app.documents.core.network.room.models.RequestCreateRoom
import app.documents.core.network.room.models.RequestCreateTag
import app.documents.core.network.room.models.RequestDeleteRoom
import app.documents.core.network.room.models.RequestRenameRoom
import app.documents.core.network.room.models.RequestSetLogo
import app.documents.core.network.room.models.RequestUpdateExternalLink
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.request.Invitation
import app.documents.core.network.share.models.request.RequestRoomShare
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.FileUtils.toByteArray
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject

class RoomProvider @Inject constructor(private val roomService: RoomService) {

    fun archiveRoom(id: String, isArchive: Boolean = true): Observable<BaseResponse> {
        return if (isArchive) {
            roomService.archive(id, RequestArchive())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        } else {
            roomService.unarchive(id, RequestArchive())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        }

    }

    fun pinRoom(id: String, isPin: Boolean = true): Observable<BaseResponse> {
        return if (isPin) {
            roomService.pinRoom(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        } else {
            roomService.unpinRoom(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        }

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

    fun deleteRoom(id: String = "", items: List<String>? = null): Observable<BaseResponse> {
        return if (items != null && id.isEmpty()) {
            Observable.fromIterable(items)
                .subscribeOn(Schedulers.io())
                .flatMap { itemId -> roomService.deleteRoom(itemId, RequestDeleteRoom()) }
                .map { it.body() }
                .buffer(items.size)
                .observeOn(AndroidSchedulers.mainThread())
                .map { responses -> responses[0] }
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
        tags.forEach { newTag ->
            if (!existTags.contains(newTag)) {
                roomService.createTag(RequestCreateTag(newTag))
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

    suspend fun getExternalLinks(id: String): List<ExternalLink> {
        val response = roomService.getExternalLinks(id)
        val body = response.body()
        return if (response.isSuccessful && body != null) body.response else throw HttpException(response)
    }

    suspend fun updateExternalLink(
        roomId: String,
        access: Int,
        linkId: String,
        linkType: Int,
        denyDownload: Boolean,
        expirationDate: String?,
        password: String?,
        title: String
    ): ExternalLink {
        val request = RequestUpdateExternalLink(
            access = access,
            denyDownload = denyDownload,
            expirationDate = expirationDate,
            linkId = linkId,
            linkType = linkType,
            password = password,
            title = title
        )
        val response = roomService.updateExternalLink(roomId, request)
        val body = response.body()
        return if (response.isSuccessful && body != null) body.response else throw HttpException(response)
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
        val logId = UUID.randomUUID().toString()
        val uploadResponse = roomService.uploadLogo(
            MultipartBody.Part.createFormData(
                logId,
                "$logId.png",
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

}