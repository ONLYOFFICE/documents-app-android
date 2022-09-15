package app.documents.core.room

import app.documents.core.network.ApiContract
import app.documents.core.network.models.Base
import app.documents.core.network.models.room.RequestArchive
import app.documents.core.network.models.room.RequestCreateRoom
import app.documents.core.network.models.room.RequestRenameRoom
import app.documents.core.network.models.room.RequestSendLinks
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface RoomApi {

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