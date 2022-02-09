package app.editors.manager.storages.onedrive.onedrive

import app.editors.manager.storages.onedrive.mvp.models.request.*
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response


sealed class OneDriveResponse {
    class Success(val response: Any) : OneDriveResponse()
    class Error(val error: Throwable) : OneDriveResponse()
}

interface IOneDriveServiceProvider {

    fun authorization(parameters: Map<String, String>): Single<OneDriveResponse>
    fun getFiles(map: Map<String, String>): Single<OneDriveResponse>
    fun getChildren(itemId: String, map:  Map<String, String>): Single<OneDriveResponse>
    fun getRoot(): Single<OneDriveResponse>
    fun download(itemId: String): Single<OneDriveResponse>
    fun deleteItem(itemId: String): Single<Response<ResponseBody>>
    fun renameItem(itemId: String, request: RenameRequest): Single<Response<ResponseBody>>
    fun createFolder(itemId: String, request: CreateFolderRequest): Single<OneDriveResponse>
    fun createFile(itemId: String, fileName: String, opts: Map<String, String>): Single<OneDriveResponse>
    fun updateFile(itemId: String, request: ChangeFileRequest):Single<Response<ResponseBody>>
    fun uploadFile(folderId: String, fileName: String, request: UploadRequest): Single<OneDriveResponse>
    fun copyItem(itemId: String, request: CopyItemRequest): Single<Response<ResponseBody>>
    fun moveItem(itemId: String, request: CopyItemRequest): Single<Response<ResponseBody>>
    fun getPhoto(): Single<Response<ResponseBody>>
    fun filter(value: String, map: Map<String, String>): Single<OneDriveResponse>
    fun getExternalLink(itemId: String, request: ExternalLinkRequest): Single<OneDriveResponse>
}