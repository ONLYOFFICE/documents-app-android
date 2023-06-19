package app.documents.core.providers

import android.net.Uri
import app.documents.core.network.manager.models.explorer.*
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestExternal
import app.documents.core.network.manager.models.response.ResponseExternal
import app.documents.core.network.manager.models.response.ResponseOperation
import io.reactivex.Observable

interface BaseFileProvider : CacheFileHelper {
    fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer>
    fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile>
    fun search(query: String?): Observable<String>?
    fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder>
    fun rename(item: Item, newName: String, version: Int?): Observable<Item>
    fun delete(items: List<Item>, from: CloudFolder?): Observable<List<Operation>>
    fun transfer(
        items: List<Item>,
        to: CloudFolder,
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
}