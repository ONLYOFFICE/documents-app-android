package app.documents.core.providers

import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.Operation
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.response.ResponseOperation
import io.reactivex.Observable

interface BaseFileProvider : CacheFileHelper {
    fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer>
    fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile>
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
    fun terminate(): Observable<List<Operation>>?
}