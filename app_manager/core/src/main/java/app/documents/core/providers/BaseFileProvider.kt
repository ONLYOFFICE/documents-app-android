package app.documents.core.providers

import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.Operation
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.response.ResponseOperation
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import lib.toolkit.base.managers.utils.EditType
import java.io.File

sealed class FileOpenResult {

    class Loading : FileOpenResult()
    class Failed(val throwable: Throwable) : FileOpenResult()
    class DownloadNotSupportedFile : FileOpenResult()
    class OpenMedia(val fileId: String) : FileOpenResult()
    class OpenDocumentServer(
        val cloudFile: CloudFile,
        val info: String,
        val editType: EditType
    ) : FileOpenResult()
    class OpenLocally(val file: File, val fileId: String, val editType: EditType) : FileOpenResult()
}

interface BaseFileProvider : CacheFileHelper {

    val fileOpenResultFlow: Flow<FileOpenResult>

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