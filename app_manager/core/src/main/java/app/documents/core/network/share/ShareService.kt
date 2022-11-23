package app.documents.core.network.share

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.share.models.request.*
import app.documents.core.network.share.models.response.ResponseExternal
import app.documents.core.network.share.models.response.ResponseGroups
import app.documents.core.network.share.models.response.ResponseShare
import app.documents.core.network.share.models.response.ResponseUsers
import retrofit2.http.*

interface ShareService {

    /*
     * Get share folder
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/folder/{folder_id}/share" + ApiContract.RESPONSE_FORMAT)
    suspend fun getShareFolder(@Path(value = "folder_id") folderId: String): ResponseShare

    /*
     * Get share file
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}/share")
    suspend fun getShareFile(@Path(value = "file_id") fileId: String): ResponseShare

    /*
     * Get external link
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/{file_id}/sharedlink" + ApiContract.RESPONSE_FORMAT)
    suspend fun getExternalLink(
        @Path(value = "file_id") fileId: String,
        @Body body: RequestExternal
    ): ResponseExternal

    /*
     * Set external link access
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/{file_id}/setacelink" + ApiContract.RESPONSE_FORMAT)
    suspend fun setExternalLinkAccess(
        @Path(value = "file_id") fileId: String,
        @Body body: RequestExternalAccess
    ): BaseResponse

    /*
     * Set access for folder
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/folder/{folder_id}/share" + ApiContract.RESPONSE_FORMAT)
    suspend fun setFolderAccess(
        @Path(value = "folder_id") fileId: String,
        @Body body: RequestShare
    ): ResponseShare

    /*
     * Set access for file
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{file_id}/share")
    suspend fun setFileAccess(
        @Path(value = "file_id") fileId: String,
        @Body body: RequestShare
    ): ResponseShare

    /*
     * Delete share setting
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/share" + ApiContract.RESPONSE_FORMAT,
        hasBody = true
    )
    suspend fun deleteShare(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @Body body: RequestDeleteShare
    ): BaseResponse

    /*
     * Get groups
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/group" + ApiContract.RESPONSE_FORMAT)
    suspend fun getGroups(
        @QueryMap options: Map<String, String> = mapOf()
    ): ResponseGroups

    /*
     * Get users
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/people" + ApiContract.RESPONSE_FORMAT)
    suspend fun getUsers(
        @QueryMap options: Map<String, String> = mapOf()
    ): ResponseUsers

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/rooms/{id}/share")
    suspend fun shareRoom(
        @Path(value = "id") id: String,
        @Body body: RequestRoomShare
    ): ResponseShare

}