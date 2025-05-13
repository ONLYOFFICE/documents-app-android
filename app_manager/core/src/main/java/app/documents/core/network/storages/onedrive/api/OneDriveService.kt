package app.documents.core.network.storages.onedrive.api

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemCloudTree
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemValue
import app.documents.core.network.storages.onedrive.models.request.ChangeFileRequest
import app.documents.core.network.storages.onedrive.models.request.CopyItemRequest
import app.documents.core.network.storages.onedrive.models.request.CreateFolderRequest
import app.documents.core.network.storages.onedrive.models.request.ExternalLinkRequest
import app.documents.core.network.storages.onedrive.models.request.RenameRequest
import app.documents.core.network.storages.onedrive.models.request.UploadRequest
import app.documents.core.network.storages.onedrive.models.response.ExternalLinkResponse
import app.documents.core.network.storages.onedrive.models.response.UploadResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface OneDriveService {

    companion object {
        private const val API_VERSION = "beta"
    }

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION/me/drive/root/children")
    fun getFiles(@QueryMap map: Map<String, String>): Single<Response<DriveItemCloudTree>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION/me/drive/items/{item_id}/children")
    fun getChildren(
        @Path(value = "item_id") id: String,
        @QueryMap map: Map<String, String>
    ): Single<Response<DriveItemCloudTree>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION/me/drive/root")
    fun getRoot(): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION/me/drive/items/{item-id}/content")
    fun download(@Path(value = "item-id") itemId: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION/me/drive/items/{item-id}/content")
    suspend fun suspendDownload(@Path(value = "item-id") itemId: String): Response<ResponseBody>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE("$API_VERSION/me/drive/items/{item_id}")
    fun deleteItem(@Path(value = "item_id") item_id: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH("$API_VERSION/me/drive/items/{item_id}")
    fun renameItem(
        @Path(value = "item_id") itemId: String,
        @Body request: RenameRequest
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("$API_VERSION/me/drive/items/{parent_item_id}/children")
    fun createFolder(
        @Path(value = "parent_item_id") itemId: String,
        @Body request: CreateFolderRequest
    ): Single<Response<DriveItemValue>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("$API_VERSION/me/drive/items/{parent_item_id}:/{file_name}:/content")
    fun createFile(
        @Path(value = "parent_item_id") itemId: String,
        @Path(value = "file_name") fileName: String,
        @QueryMap map: Map<String, String>
    ): Single<Response<DriveItemValue>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": text/plain",
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("$API_VERSION/me/drive/items/{item_id}/content")
    fun updateFile(
        @Path(value = "item_id") itemId: String,
        @Body request: ChangeFileRequest
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("$API_VERSION/me/drive/items/{folder_id}:/{file_name}:/createUploadSession")
    fun uploadFile(
        @Path(value = "folder_id") folderId: String,
        @Path(value = "file_name") fileName: String,
        @Body request: UploadRequest
    ): Single<Response<UploadResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("$API_VERSION/me/drive/items/{item_id}/copy")
    fun copyItem(
        @Path(value = "item_id") itemId: String,
        @Body request: CopyItemRequest
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH("$API_VERSION/me/drive/items/{item_id}")
    fun moveItem(
        @Path(value = "item_id") itemId: String,
        @Body request: CopyItemRequest
    ): Single<Response<ResponseBody>>


    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION/me/photo")
    fun getPhoto(): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION/me/drive/root/search(q='{search_text}')")
    fun filter(
        @Path(value = "search_text") value: String,
        @QueryMap map: Map<String, String>
    ): Single<Response<DriveItemCloudTree>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("$API_VERSION/me/drive/items/{item_id}/createLink")
    fun getExternalLink(
        @Path(value = "item_id") itemId: String,
        @Body request: ExternalLinkRequest
    ): Single<Response<ExternalLinkResponse>>

}