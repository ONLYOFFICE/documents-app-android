package app.editors.manager.app

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.Base
import app.documents.core.network.room.models.*
import app.editors.manager.mvp.models.response.ResponseExplorer
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface RoomApi {

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
    fun archive(@Path(value = "id") id: String, @Body body: RequestArchive): Observable<Response<Base>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/unarchive")
    fun unarchive(@Path(value = "id") id: String, @Body body: RequestArchive): Observable<Response<Base>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/rooms/{id}",
        hasBody = true
    )
    fun deleteRoom(@Path(value = "id") id: String, @Body body: RequestDeleteRoom): Observable<Response<Base>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/pin")
    fun pinRoom(@Path(value = "id") id: String): Observable<Response<Base>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/unpin")
    fun unpinRoom(@Path(value = "id") id: String): Observable<Response<Base>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}")
    fun renameRoom(@Path(value = "id") id: String, @Body body: RequestRenameRoom): Observable<Response<Base>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/rooms")
    fun createRoom(@Body body: RequestCreateRoom): Observable<Response<Base>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/links/send")
    fun sendLink(@Path("id") id: String, @Body body: RequestSendLinks): Observable<Response<Base>>

}