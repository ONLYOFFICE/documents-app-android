package app.editors.manager.storages.dropbox.dropbox.api

import app.documents.core.network.ApiContract
import app.editors.manager.storages.dropbox.managers.utils.DropboxUtils
import app.editors.manager.storages.dropbox.mvp.models.explorer.DropboxItem
import app.editors.manager.storages.dropbox.mvp.models.operations.MoveCopyBatchCheck
import app.editors.manager.storages.dropbox.mvp.models.request.*
import app.editors.manager.storages.dropbox.mvp.models.response.*
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface DropboxService {

    companion object {
        const val API_VERSION = "2/"
        const val DROPBOX_BASE_URL = "https://api.dropboxapi.com/"
        const val DROPBOX_BASE_URL_CONTENT = "https://content.dropboxapi.com/"
    }

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/list_folder")
    fun getFiles(@Body request: ExplorerRequest): Single<Response<ExplorerResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/list_folder/continue")
    fun getNextFileList(@Body request: ExplorerContinueRequest): Single<Response<ExplorerResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/download")
    fun download(@Header(DropboxUtils.DROPBOX_API_ARG_HEADER) request: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/download_zip")
    fun downloadFolder(@Header(DropboxUtils.DROPBOX_API_ARG_HEADER) request: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/delete_v2")
    fun delete(@Body request: PathRequest): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/create_folder_v2")
    fun createFolder(@Body request: CreateFolderRequest): Single<Response<MetadataResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/get_temporary_link")
    fun getExternalLink(@Body request: PathRequest): Single<Response<ExternalLinkResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/move_v2")
    fun move(@Body request: MoveRequest): Single<Response<MetadataResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/move_batch_v2")
    fun moveBatch(@Body request: MoveCopyBatchRequest): Single<Response<MoveCopyBatchResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/copy_v2")
    fun copy(@Body request: MoveRequest): Single<Response<MetadataResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/copy_batch_v2")
    fun copyBatch(@Body request: MoveCopyBatchRequest): Single<Response<MoveCopyBatchResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/copy_batch/check_v2")
    fun copyBatchCheck(@Body request: MoveCopyBatchCheck): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/move_batch/check_v2")
    fun moveBatchCheck(@Body request: MoveCopyBatchCheck): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/search_v2")
    fun search(@Body request: SearchRequest): Single<Response<SearchResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/search/continue_v2")
    fun searchNextList(@Body request: ExplorerContinueRequest): Single<Response<SearchResponse>>

    @Multipart
    @Headers("Content-Type:application/octet-stream")
    @POST("${API_VERSION}files/upload")
    fun upload(@Header(DropboxUtils.DROPBOX_API_ARG_HEADER) request: String , @Part part: MultipartBody.Part): Single<Response<DropboxItem>>
}