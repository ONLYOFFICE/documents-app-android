package app.editors.manager.managers.providers

import android.net.Uri
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.models.request.RequestExternal
import app.editors.manager.mvp.models.request.RequestFavorites
import app.editors.manager.mvp.models.response.ResponseExternal
import app.editors.manager.mvp.models.response.ResponseOperation
import io.reactivex.Observable

interface BaseFileProvider {
    fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer>
    fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile>
    fun search(query: String?): Observable<String>?
    fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder>
    fun rename(item: Item, newName: String, version: Int?): Observable<Item>
    fun delete(items: List<Item>, from: CloudFolder?): Observable<List<Operation>>
    fun transfer(
        items: List<Item>,
        to: CloudFolder?,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>>?

    fun fileInfo(item: Item?): Observable<CloudFile>
    fun getStatusOperation(): ResponseOperation?
    fun download(items: List<Item>): Observable<Int>?
    fun upload(folderId: String, uris: List<Uri?>): Observable<Int>?
    fun share(id: String, requestExternal: RequestExternal): Observable<ResponseExternal>?
    fun terminate(): Observable<List<Operation>>?
    fun addToFavorites(requestFavorites: RequestFavorites): Observable<Base>?
    fun deleteFromFavorites(requestFavorites: RequestFavorites): Observable<Base>?
}