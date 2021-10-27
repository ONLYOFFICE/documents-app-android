package app.editors.manager.dropbox.dropbox.api

import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.mvp.models.request.*
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response

interface IDropboxServiceProvider {
    fun getFiles(request: ExplorerRequest): Single<DropboxResponse>
    fun download(request: String): Single<Response<ResponseBody>>
    fun downloadFolder(request: String): Single<Response<ResponseBody>>
    fun delete(request: PathRequest): Single<Response<ResponseBody>>
    fun createFolder(request: CreateFolderRequest): Single<DropboxResponse>
    fun getExternalLink(request: PathRequest): Single<DropboxResponse>
    fun move(request: MoveRequest): Single<DropboxResponse>
    fun search(request: SearchRequest): Single<DropboxResponse>
    fun upload(request: String, part: MultipartBody.Part): Single<DropboxResponse>
}