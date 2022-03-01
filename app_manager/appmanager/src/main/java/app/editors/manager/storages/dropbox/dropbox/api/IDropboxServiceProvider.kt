package app.editors.manager.storages.dropbox.dropbox.api

import app.editors.manager.storages.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.storages.dropbox.mvp.models.operations.MoveCopyBatchCheck
import app.editors.manager.storages.dropbox.mvp.models.request.*
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response

interface IDropboxServiceProvider {
    fun getFiles(request: ExplorerRequest): Single<DropboxResponse>
    fun getNextFileList(request: ExplorerContinueRequest): Single<DropboxResponse>
    fun download(request: String): Single<Response<ResponseBody>>
    fun downloadFolder(request: String): Single<Response<ResponseBody>>
    fun delete(request: PathRequest): Single<Response<ResponseBody>>
    fun createFolder(request: CreateFolderRequest): Single<DropboxResponse>
    fun getExternalLink(request: PathRequest): Single<DropboxResponse>
    fun move(request: MoveRequest): Single<DropboxResponse>
    fun moveBatch(request: MoveCopyBatchRequest): Single<DropboxResponse>
    fun copy(request: MoveRequest): Single<DropboxResponse>
    fun copyBatch(request: MoveCopyBatchRequest): Single<DropboxResponse>
    fun copyBatchCheck(request: MoveCopyBatchCheck): Single<Response<ResponseBody>>
    fun moveBatchCheck(request: MoveCopyBatchCheck): Single<Response<ResponseBody>>
    fun search(request: SearchRequest): Single<DropboxResponse>
    fun searchNextList(request: ExplorerContinueRequest): Single<DropboxResponse>
    fun upload(request: String, part: MultipartBody.Part): Single<DropboxResponse>
}