package app.documents.core.providers

import android.graphics.Bitmap
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.room.RoomService
import app.documents.core.network.room.models.RequestAddTags
import app.documents.core.network.room.models.RequestArchive
import app.documents.core.network.room.models.RequestCreateRoom
import app.documents.core.network.room.models.RequestCreateTag
import app.documents.core.network.room.models.RequestDeleteRoom
import app.documents.core.network.room.models.RequestRenameRoom
import app.documents.core.network.room.models.RequestRoomOwner
import app.documents.core.network.room.models.RequestSetLogo
import app.documents.core.network.share.models.request.Invitation
import app.documents.core.network.share.models.request.RequestRoomShare
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
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
                    tmpFile = JSONObject(uploadResponse.body()?.string() ?: "").optJSONObject("response")?.optString("data")
                        ?: "",
                    width = logo.width,
                    height = logo.height
                )
            )
        }
    }

    suspend fun setRoomOwner(id: String, userId: String, ownerId: String) {
        val resultSetOwner = roomService.setOwner(RequestRoomOwner(userId, listOf(id)))
        delay(200)
        val resultShare = roomService.shareRoom(id, RequestRoomShare(
            listOf(Invitation(id = ownerId, access = ApiContract.ShareCode.NONE))
        ))
        if (!resultSetOwner.isSuccessful) throw HttpException(resultSetOwner)
        if (!resultShare.isSuccessful) throw HttpException(resultShare)
    }

}