package app.documents.core.providers

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.room.RoomService
import app.documents.core.network.room.models.RequestArchive
import app.documents.core.network.room.models.RequestCreateRoom
import app.documents.core.network.room.models.RequestDeleteRoom
import app.documents.core.network.room.models.RequestRenameRoom
import app.documents.core.network.share.models.ExternalLink
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
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

    fun renameRoom(id: String, newTitle: String): Observable<BaseResponse> {
        return roomService.renameRoom(id, RequestRenameRoom(newTitle))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.body() }
    }

    fun createRoom(title: String, type: Int): Observable<CloudFolder> {
        return roomService.createRoom(
            RequestCreateRoom(
                title = title,
                roomType = type
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.response }

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

    suspend fun getExternalLinks(id: String): List<ExternalLink> {
        val response = roomService.getExternalLinks(id)
        val body = response.body()
        return if (response.isSuccessful && body != null) body.response else throw HttpException(response)
    }

}