package app.documents.core.network.storages.googledrive.api

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.storages.googledrive.models.GoogleDriveFile
import app.documents.core.network.storages.googledrive.models.request.CreateItemRequest
import app.documents.core.network.storages.googledrive.models.request.RenameRequest
import app.documents.core.network.storages.googledrive.models.request.ShareRequest
import app.documents.core.network.storages.googledrive.models.resonse.GoogleDriveExplorerResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface GoogleDriveService {

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/files")
    fun getFiles(
        @QueryMap stringMap: Map<String, String> = mapOf(),
        @QueryMap integerMap: Map<String, Int> = mapOf()
    ): Single<Response<GoogleDriveExplorerResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE("drive/v3/files/{fileId}")
    fun deleteItem(@Path(value = "fileId") fileId: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/files/{fileId}")
    fun getFileInfo(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String>): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/files/{fileId}")
    fun download(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String>): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH("drive/v3/files/{fileId}")
    fun rename(@Path(value = "fileId") fileId: String, @Body request: RenameRequest): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("drive/v3/files/{fileId}/permissions")
    fun share(@Path(value = "fileId") fileId: String, @Body request: ShareRequest): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH("drive/v3/files/{fileId}")
    fun move(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String?>): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("drive/v3/files/{fileId}/copy")
    fun copy(@Path(value = "fileId") fileId: String): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("drive/v3/files")
    fun createItem(@Body request: CreateItemRequest): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("upload/drive/v3/files?uploadType=resumable")
    fun upload(@Body  request: CreateItemRequest, @QueryMap map: Map<String, String>): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH("upload/drive/v3/files/{fileId}?uploadType=resumable")
    fun update(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String>): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/files/{fileId}/export")
    fun export(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String>): Single<Response<ResponseBody>>

}