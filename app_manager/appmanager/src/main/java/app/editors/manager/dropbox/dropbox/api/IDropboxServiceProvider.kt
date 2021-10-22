package app.editors.manager.dropbox.dropbox.api

import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.mvp.models.request.CreateFolderRequest
import app.editors.manager.dropbox.mvp.models.request.DeleteRequest
import app.editors.manager.dropbox.mvp.models.request.ExplorerRequest
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response

interface IDropboxServiceProvider {
    fun getFiles(request: ExplorerRequest): Single<DropboxResponse>
    fun download(request: ExplorerRequest): Single<DropboxResponse>
    fun delete(request: DeleteRequest): Single<Response<ResponseBody>>
    fun createFolder(request: CreateFolderRequest): Single<DropboxResponse>
}