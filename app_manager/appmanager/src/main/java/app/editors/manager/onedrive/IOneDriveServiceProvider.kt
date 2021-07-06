package app.editors.manager.managers.providers

import app.editors.manager.onedrive.CreateFolderRequest
import app.editors.manager.onedrive.RenameRequest
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response


sealed class OneDriveResponse {
    class Success(val response: Any) : OneDriveResponse()
    class Error(val error: Throwable) : OneDriveResponse()
}

interface IOneDriveServiceProvider {

    fun authorization(parameters: Map<String, String>): Single<OneDriveResponse>
    fun userInfo(): Single<OneDriveResponse>
    fun getFiles(): Single<OneDriveResponse>
    fun getChildren(itemId: String): Single<OneDriveResponse>
    fun getRoot(): Single<OneDriveResponse>
    fun download(itemId: String): Single<OneDriveResponse>
    fun deleteItem(itemId: String): Single<Response<ResponseBody>>
    fun renameItem(itemId: String, request: RenameRequest): Single<Response<ResponseBody>>
    fun createFolder(itemId: String, request: CreateFolderRequest): Single<OneDriveResponse>
}