package app.documents.core.network.room

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.response.ResponseCreateFolder
import app.documents.core.network.manager.models.response.ResponseExplorer
import app.documents.core.network.room.models.*
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface RoomService {

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/rooms/")
    fun getAllRooms(@QueryMap options: Map<String, String>?): Observable<Response<ResponseExplorer>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/archive")
    fun archive(@Path(value = "id") id: String, @Body body: RequestArchive): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/unarchive")
    fun unarchive(@Path(value = "id") id: String, @Body body: RequestArchive): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/rooms/{id}",
        hasBody = true
    )
    fun deleteRoom(@Path(value = "id") id: String, @Body body: RequestDeleteRoom): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/pin")
    fun pinRoom(@Path(value = "id") id: String): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/unpin")
    fun unpinRoom(@Path(value = "id") id: String): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}")
    fun renameRoom(@Path(value = "id") id: String, @Body body: RequestRenameRoom): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/rooms")
    suspend fun createRoom(@Body body: RequestCreateRoom): Response<ResponseCreateFolder>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/links/send")
    fun sendLink(@Path("id") id: String, @Body body: RequestSendLinks): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/tags")
    suspend fun getTags(@QueryMap options: Map<String, String>? = mapOf(
        "filterValue" to "",
        "startIndex" to "",
        "count" to ""
    )): ResponseTags

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/tags")
    suspend fun createTag(@Body body: RequestCreateTag): Response<BaseResponse>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/tags")
    suspend fun addTags(@Path("id") id: String, @Body body: RequestAddTags): Response<BaseResponse>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE("api/" + ApiContract.API_VERSION + "/files/tags")
    suspend fun deleteTags(@Body body: RequestAddTags): Response<BaseResponse>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/logo")
    fun setLogo(@Path("id") id: String, @Body body: RequestSetLogo): Single<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/logo")
    suspend fun deleteLogo(@Path("id") id: String): Response<ResponseBody>

    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/logos")
    suspend fun uploadLogo(
        @Part part: MultipartBody.Part
    ): Response<ResponseUploadLogo>

}