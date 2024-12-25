package app.documents.core.network.room

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.ExportIndexOperation
import app.documents.core.network.manager.models.explorer.Operation
import app.documents.core.network.manager.models.explorer.Quota
import app.documents.core.network.manager.models.request.RequestBatchOperation
import app.documents.core.network.manager.models.request.RequestRoomNotifications
import app.documents.core.network.manager.models.response.ResponseCreateFolder
import app.documents.core.network.manager.models.response.ResponseExplorer
import app.documents.core.network.manager.models.response.ResponseRoomNotifications
import app.documents.core.network.room.models.RequestAddTags
import app.documents.core.network.room.models.RequestArchive
import app.documents.core.network.room.models.RequestCreateExternalLink
import app.documents.core.network.room.models.RequestCreateRoom
import app.documents.core.network.room.models.RequestCreateTag
import app.documents.core.network.room.models.RequestDeleteRoom
import app.documents.core.network.room.models.RequestEditRoom
import app.documents.core.network.room.models.RequestOrder
import app.documents.core.network.room.models.RequestRoomAuthViaLink
import app.documents.core.network.room.models.RequestRoomOwner
import app.documents.core.network.room.models.RequestSendLinks
import app.documents.core.network.room.models.RequestSetLogo
import app.documents.core.network.room.models.RequestUpdateExternalLink
import app.documents.core.network.room.models.ResponseRoomAuthViaLink
import app.documents.core.network.room.models.ResponseRoomShare
import app.documents.core.network.room.models.ResponseTags
import app.documents.core.network.room.models.ResponseUpdateExternalLink
import app.documents.core.network.room.models.ResponseUploadLogo
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.GroupShare
import app.documents.core.network.share.models.request.RequestAddInviteLink
import app.documents.core.network.share.models.request.RequestCreateSharedLink
import app.documents.core.network.share.models.request.RequestCreateThirdPartyRoom
import app.documents.core.network.share.models.request.RequestRemoveInviteLink
import app.documents.core.network.share.models.request.RequestRoomShare
import app.documents.core.network.share.models.request.RequestUpdateSharedLink
import app.documents.core.network.share.models.response.ResponseExternalLink
import app.documents.core.network.share.models.response.ResponseShare
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.QueryMap

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
    suspend fun archive(
        @Path(value = "id") id: String,
        @Body body: RequestArchive
    ): Response<ResponseBody>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/unarchive")
    suspend fun unarchive(
        @Path(value = "id") id: String,
        @Body body: RequestArchive
    ): Response<ResponseBody>

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
    suspend fun editRoom(@Path(value = "id") id: String, @Body body: RequestEditRoom): Response<BaseResponse>

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
    @GET("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/link")
    suspend fun getExternalLink(
        @Path("id") id: String,
    ): app.documents.core.network.BaseResponse<ExternalLink>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/tags")
    suspend fun getTags(): ResponseTags

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
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/rooms/{id}/tags",
        hasBody = true
    )
    suspend fun deleteTagsFromRoom(@Path("id") id: String, @Body body: RequestAddTags): Response<ResponseBody>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/tags",
        hasBody = true
    )
    suspend fun deleteTags(@Body body: RequestAddTags): Response<BaseResponse>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/logo")
    suspend fun setLogo(@Path("id") id: String, @Body body: RequestSetLogo): Response<ResponseBody>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/logo")
    suspend fun deleteLogo(@Path("id") id: String): Response<ResponseBody>

    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/logos")
    suspend fun uploadLogo(
        @Part part: MultipartBody.Part,
    ): app.documents.core.network.BaseResponse<ResponseUploadLogo>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/owner")
    suspend fun setOwner(@Body body: RequestRoomOwner): app.documents.core.network.BaseResponse<List<CloudFolder>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/share?filterType=2")
    suspend fun getRoomSharedLinks(@Path("id") id: String): Response<ResponseExternalLink>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/share?filterType=1")
    suspend fun setRoomInviteLink(@Path("id") id: String): app.documents.core.network.BaseResponse<List<ExternalLink>?>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/links")
    suspend fun removeRoomInviteLink(
        @Path("id") id: String,
        @Body request: RequestRemoveInviteLink,
    ): app.documents.core.network.BaseResponse<ExternalLink?>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/links")
    suspend fun addRoomInviteLink(
        @Path("id") id: String,
        @Body request: RequestAddInviteLink,
    ): app.documents.core.network.BaseResponse<ExternalLink>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/links")
    suspend fun updateRoomSharedLink(
        @Path("id") id: String,
        @Body request: RequestUpdateExternalLink,
    ): Response<ResponseUpdateExternalLink>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/links")
    suspend fun createRoomSharedLink(
        @Path("id") id: String,
        @Body request: RequestCreateExternalLink,
    ): Response<ResponseUpdateExternalLink>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{id}/links")
    suspend fun createSharedLink(
        @Path("id") id: String,
        @Body body: RequestCreateSharedLink,
    ): app.documents.core.network.BaseResponse<ExternalLink>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{id}/links")
    suspend fun getSharedLinks(@Path("id") id: String): Response<ResponseExternalLink>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{id}/links")
    suspend fun updateSharedLink(
        @Path("id") id: String,
        @Body body: RequestUpdateSharedLink,
    ): app.documents.core.network.BaseResponse<ExternalLink>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/share?filterType=0")
    suspend fun getRoomUsers(@Path("id") id: String): Response<ResponseShare>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/share")
    suspend fun setRoomUserAccess(@Path("id") id: String, @Body body: RequestRoomShare): Response<ResponseRoomShare>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/share")
    suspend fun shareRoom(
        @Path(value = "id") id: String,
        @Body body: RequestRoomShare,
    ): Response<ResponseBody>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/rooms/thirdparty/{id}")
    suspend fun createThirdPartyRoom(
        @Path(value = "id") id: String,
        @Body body: RequestCreateThirdPartyRoom,
    ): Response<ResponseCreateFolder>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/folder/{roomId}/group/{groupId}/share")
    suspend fun getGroupUsers(
        @Path("roomId") roomId: String,
        @Path("groupId") groupId: String,
    ): app.documents.core.network.BaseResponse<List<GroupShare>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/copy")
    suspend fun copy(@Body body: RequestBatchOperation): app.documents.core.network.BaseResponse<List<Operation>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/fileops")
    suspend fun status(): app.documents.core.network.BaseResponse<List<Operation>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/duplicate")
    suspend fun duplicate(@Body body: RequestBatchOperation): app.documents.core.network.BaseResponse<List<Operation>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/settings/notification/rooms")
    suspend fun muteNotifications(@Body body: RequestRoomNotifications): app.documents.core.network.BaseResponse<ResponseRoomNotifications>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/portal/payment/quota")
    suspend fun getQuota(): app.documents.core.network.BaseResponse<Quota>

    @GET("api/" + ApiContract.API_VERSION + "/files/rooms/{roomId}")
    suspend fun getRoomInfo(
        @Path("roomId") roomId: String
    ): app.documents.core.network.BaseResponse<CloudFolder>

    @PUT("api/" + ApiContract.API_VERSION + "/files/order")
    suspend fun order(@Body body: RequestOrder): Response<ResponseBody>

    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{roomId}/reorder")
    suspend fun reorder(
        @Path("roomId") roomId: String
    ): Response<ResponseBody>

    @GET("api/" + ApiContract.API_VERSION + "/files/rooms/indexexport")
    suspend fun getIndexExportProgress(): app.documents.core.network.BaseResponse<ExportIndexOperation>

    @POST("api/" + ApiContract.API_VERSION + "/files/rooms/{roomId}/indexexport")
    suspend fun startIndexExport(
        @Path("roomId") roomId: String
    ): Response<ResponseBody>

    @POST("api/" + ApiContract.API_VERSION + "/files/share/{token}/password")
    suspend fun authRoomViaLink(
        @Path("token") requestToken: String,
        @Body body: RequestRoomAuthViaLink
    ): app.documents.core.network.BaseResponse<ResponseRoomAuthViaLink>
}