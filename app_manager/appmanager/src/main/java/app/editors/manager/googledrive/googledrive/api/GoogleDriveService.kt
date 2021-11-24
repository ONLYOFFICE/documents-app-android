package app.editors.manager.googledrive.googledrive.api

import app.documents.core.network.ApiContract
import app.editors.manager.googledrive.mvp.models.GoogleDriveFile
import app.editors.manager.googledrive.mvp.models.request.CreateItemRequest
import app.editors.manager.googledrive.mvp.models.request.RenameRequest
import app.editors.manager.googledrive.mvp.models.resonse.GoogleDriveExplorerResponse
import io.reactivex.Observable
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
}