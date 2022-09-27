package app.editors.manager.storages.googledrive.googledrive.api

import app.documents.core.network.ApiContract
import app.editors.manager.storages.googledrive.mvp.models.GoogleDriveFile
import app.editors.manager.storages.googledrive.mvp.models.request.CreateItemRequest
import app.editors.manager.storages.googledrive.mvp.models.request.RenameRequest
import app.editors.manager.storages.googledrive.mvp.models.request.ShareRequest
import app.editors.manager.storages.googledrive.mvp.models.resonse.GoogleDriveExplorerResponse
import app.editors.manager.storages.googledrive.mvp.models.resonse.UserResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GoogleDriveService {

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/files")
    fun getFiles(@QueryMap map: Map<String,String>): Single<Response<GoogleDriveExplorerResponse>>


    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE("drive/v3/files/{fileId}")
    fun deleteItem(@Path(value = "fileId") fileId: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/files/{fileId}")
    fun getFileInfo(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String>): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/files/{fileId}")
    fun download(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String>): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH("drive/v3/files/{fileId}")
    fun rename(@Path(value = "fileId") fileId: String, @Body request: RenameRequest): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("drive/v3/files/{fileId}/permissions")
    fun share(@Path(value = "fileId") fileId: String, @Body request: ShareRequest): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH("drive/v3/files/{fileId}")
    fun move(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String?>): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("drive/v3/files/{fileId}/copy")
    fun copy(@Path(value = "fileId") fileId: String): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("drive/v3/files")
    fun createItem(@Body request: CreateItemRequest): Single<Response<GoogleDriveFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("upload/drive/v3/files")
    fun upload(@Body  request: CreateItemRequest, @QueryMap map: Map<String, String>): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH("upload/drive/v3/files/{fileId}")
    fun update(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String>): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/files/{fileId}/export")
    fun export(@Path(value = "fileId") fileId: String, @QueryMap map: Map<String, String>): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/about/")
    fun getUserInfo(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @QueryMap map: Map<String, String> = mapOf("fields" to "user")
    ): Single<Response<UserResponse>>

}