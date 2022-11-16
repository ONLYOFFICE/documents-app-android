package app.editors.manager.managers.providers

import app.documents.core.network.common.models.Base
import app.documents.core.network.room.models.RequestArchive
import app.documents.core.network.room.models.RequestCreateRoom
import app.documents.core.network.room.models.RequestDeleteRoom
import app.documents.core.network.room.models.RequestRenameRoom
import app.editors.manager.app.RoomApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RoomProvider(private val roomApi: RoomApi) {

    fun archiveRoom(id: String, isArchive: Boolean = true): Observable<Base> {
        return if (isArchive) {
            roomApi.archive(id, RequestArchive())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        } else {
            roomApi.unarchive(id, RequestArchive())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        }

    }

    fun pinRoom(id: String, isPin: Boolean = true): Observable<Base> {
        return if (isPin) {
            roomApi.pinRoom(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        } else {
            roomApi.unpinRoom(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        }

    }

    fun renameRoom(id: String, newTitle: String): Observable<Base> {
        return roomApi.renameRoom(id, RequestRenameRoom(newTitle))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.body() }
    }

    fun createRoom(title: String, type: Int): Observable<Base> {
        return roomApi.createRoom(
            RequestCreateRoom(
                title = title,
                roomType = type
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.body() }

    }

    fun deleteRoom(id: String = "", items: List<String>? = null): Observable<Base> {
        return if (items != null && id.isEmpty()) {
            Observable.fromIterable(items)
                .subscribeOn(Schedulers.io())
                .flatMap { itemId -> roomApi.deleteRoom(itemId, RequestDeleteRoom()) }
                .map { it.body() }
                .buffer(items.size)
                .observeOn(AndroidSchedulers.mainThread())
                .map { responses -> responses[0] }
        } else if (id.isNotEmpty()) {
            roomApi.deleteRoom(id, RequestDeleteRoom())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() }
        } else {
            Observable.empty()
        }

    }

}