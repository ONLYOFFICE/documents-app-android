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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.FileUtils.toByteArray
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    suspend fun createRoom(title: String, type: Int, tags: List<String>?, logo: Bitmap?): String {
        val response = roomService.createRoom(
            RequestCreateRoom(
                title = title,
                roomType = type
            )
        )
        if (response.isSuccessful) {
            if (!tags.isNullOrEmpty()) {
                response.body()?.response?.id?.let {
                    roomService.addTags(it, RequestAddTags(tags.toTypedArray()))
                }
            }
            if (logo != null) {
                val uploadResponse = roomService.uploadLogo(
                    MultipartBody.Part.createFormData(
                        "logo",
                        "logo.png",
                        RequestBody.create(MediaType.get("image/*"), logo.toByteArray())
                    )
                )
                if (uploadResponse.isSuccessful) {
                    response.body()?.response?.id?.let {
                        roomService.setLogo(
                            it,
                            RequestSetLogo(uploadResponse.body()?.data ?: "", width = logo.width, height = logo.height)
                        )
                    }
                }
            }
        }
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

    suspend fun createTag(tag: String): Boolean {
        val allTags = roomService.getTags().tags
        if (allTags.contains(tag)) return false
        return roomService.createTag(RequestCreateTag(name = tag)).isSuccessful
    }

    suspend fun deleteTag(id: String? = null, tag: String): Boolean {
        return if (roomService.getTags().tags.contains(tag)) {
            if (id != null) {
                roomService.deleteTagsFromRoom(id, RequestAddTags(arrayOf(tag))).isSuccessful
            } else {
                roomService.deleteTags(RequestAddTags(arrayOf(tag))).isSuccessful
            }
        } else {
            true
        }
    }

    suspend fun deleteLogo(id: String): Boolean {
        return roomService.deleteLogo(id).isSuccessful
    }

}