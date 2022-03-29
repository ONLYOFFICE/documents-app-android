package app.editors.manager.storages.onedrive.onedrive.api

import app.documents.core.network.ApiContract
import app.editors.manager.storages.onedrive.mvp.models.explorer.DriveItemCloudTree
import app.editors.manager.storages.onedrive.mvp.models.explorer.DriveItemValue
import app.editors.manager.storages.onedrive.mvp.models.request.*
import app.editors.manager.storages.onedrive.mvp.models.response.ExternalLinkResponse
import app.editors.manager.storages.onedrive.mvp.models.response.UploadResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OneDriveService {

    companion object {
        const val API_VERSION = "beta/"
        const val ONEDRIVE_BASE_URL = "https://graph.microsoft.com/"
        const val ONEDRIVE_AUTH_URL = "https://login.microsoftonline.com/"
    }
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + "application/x-www-form-urlencoded",
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("authorization" + ApiContract.RESPONSE_FORMAT)
    fun authorization(@QueryMap parameters: Map<String, String>): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION" + "me/drive/root/children")
    fun getFiles(@QueryMap map: Map<String, String>): Single<Response<DriveItemCloudTree>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION" + "me/drive/items/{item_id}/children")
    fun getChildren(@Path(value = "item_id") id: String, @QueryMap map: Map<String, String>): Single<Response<DriveItemCloudTree>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION" + "me/drive/root")
    fun getRoot(): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION" + "me/drive/items/{item-id}/content")
    fun download(@Path(value = "item-id") itemId: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE(API_VERSION + "me/drive/items/{item_id}")
    fun deleteItem(@Path(value = "item_id")item_id: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH( API_VERSION + "me/drive/items/{item_id}")
    fun renameItem(@Path(value = "item_id") itemId: String, @Body request: RenameRequest):Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST(API_VERSION + "me/drive/items/{parent_item_id}/children")
    fun createFolder(@Path(value = "parent_item_id") itemId: String, @Body request: CreateFolderRequest): Single<Response<DriveItemValue>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT( API_VERSION + "me/drive/items/{parent_item_id}:/{file_name}:/content" )
    fun createFile(@Path(value = "parent_item_id") itemId: String, @Path(value = "file_name") fileName: String, @QueryMap map: Map<String, String> ): Single<Response<DriveItemValue>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": text/plain",
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT(API_VERSION + "me/drive/items/{item_id}/content")
    fun updateFile(@Path(value = "item_id") itemId: String, @Body request: ChangeFileRequest):Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST(API_VERSION + "me/drive/items/{folder_id}:/{file_name}:/createUploadSession")
    fun uploadFile(@Path(value = "folder_id") folderId: String, @Path(value = "file_name") fileName:String, @Body request: UploadRequest): Single<Response<UploadResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST(API_VERSION + "me/drive/items/{item_id}/copy")
    fun copyItem(@Path(value = "item_id") itemId: String, @Body request: CopyItemRequest): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PATCH(API_VERSION + "me/drive/items/{item_id}")
    fun moveItem(@Path(value = "item_id") itemId: String, @Body request: CopyItemRequest): Single<Response<ResponseBody>>


    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET(API_VERSION + "me/photo")
    fun getPhoto():Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET(API_VERSION + "me/drive/root/search(q='{search_text}')")
    fun filter(@Path(value = "search_text") value: String, @QueryMap map: Map<String, String>): Single<Response<DriveItemCloudTree>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST(API_VERSION + "me/drive/items/{item_id}/createLink")
    fun getExternalLink(@Path(value = "item_id") itemId: String, @Body request: ExternalLinkRequest): Single<Response<ExternalLinkResponse>>

}