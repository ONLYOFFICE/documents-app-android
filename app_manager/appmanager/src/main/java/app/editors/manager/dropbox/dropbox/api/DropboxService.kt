package app.editors.manager.dropbox.dropbox.api

import app.documents.core.network.ApiContract
import app.editors.manager.dropbox.managers.utils.DropboxUtils
import app.editors.manager.dropbox.mvp.models.request.*
import app.editors.manager.dropbox.mvp.models.response.ExplorerResponse
import app.editors.manager.dropbox.mvp.models.response.ExternalLinkResponse
import app.editors.manager.dropbox.mvp.models.response.MetadataResponse
import app.editors.manager.dropbox.mvp.models.response.SearchResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface DropboxService {

    companion object {
        const val API_VERSION = "2/"
        const val DROPBOX_BASE_URL = "https://api.dropboxapi.com/"
        const val DROPBOX_BASE_URL_CONTENT = "https://content.dropboxapi.com/"
    }

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/list_folder")
    fun getFiles(@Body request: ExplorerRequest): Single<Response<ExplorerResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/download")
    fun download(@Header(DropboxUtils.DROPBOX_API_ARG_HEADER) request: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/download_zip")
    fun downloadFolder(@Header(DropboxUtils.DROPBOX_API_ARG_HEADER) request: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/delete_v2")
    fun delete(@Body request: DeleteRequest): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/create_folder_v2")
    fun createFolder(@Body request: CreateFolderRequest): Single<Response<MetadataResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/get_temporary_link")
    fun getExternalLink(@Body request: DeleteRequest): Single<Response<ExternalLinkResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/move_v2")
    fun move(@Body request: MoveRequest): Single<Response<MetadataResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/search_v2")
    fun search(@Body request: SearchRequest): Single<Response<SearchResponse>>
}