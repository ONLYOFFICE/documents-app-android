package app.documents.core.providers

import app.documents.core.model.cloud.Recent
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.Operation
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.response.ResponseOperation
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import lib.toolkit.base.managers.utils.EditType
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class RecentFileProvider @Inject constructor() : BaseFileProvider {

    override val fileOpenResultFlow: Flow<FileOpenResult>
        get() = flowOf()

    override suspend fun openFile(cloudFile: CloudFile, editType: EditType, canBeShared: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        TODO("Not yet implemented")
    }

    override fun createFile(folderId: String, title: String): Observable<CloudFile> {
        TODO("Not yet implemented")
    }

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        TODO("Not yet implemented")
    }

    override fun rename(item: Item, newName: String, version: Int?): Observable<Item> {
        TODO("Not yet implemented")
    }

    override fun delete(items: List<Item>, from: CloudFolder?): Observable<List<Operation>> {
        TODO("Not yet implemented")
    }

    override fun transfer(
        items: List<Item>,
        to: CloudFolder,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>>? {
        TODO("Not yet implemented")
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        TODO("Not yet implemented")
    }

    override fun getStatusOperation(): ResponseOperation? {
        TODO("Not yet implemented")
    }

    override fun terminate(): Observable<List<Operation>>? {
        TODO("Not yet implemented")
    }

    override fun getDownloadResponse(
        cloudFile: CloudFile,
        token: String?
    ): Single<Response<ResponseBody>> {
        TODO("Not yet implemented")
    }

    suspend fun openFile(cloudFile: Recent, editType: EditType.Edit) {

    }
}