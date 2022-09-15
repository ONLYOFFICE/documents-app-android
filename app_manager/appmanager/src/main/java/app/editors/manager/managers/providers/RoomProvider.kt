package app.editors.manager.managers.providers

import app.documents.core.network.models.Base
import app.documents.core.network.models.room.RequestArchive
import app.documents.core.room.RoomApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RoomProvider(
    private val roomApi: RoomApi
) {

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

}