package app.editors.manager.onedrive

import app.documents.core.network.ApiContract
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OneDriveService {

    companion object {
        const val API_VERSION = "beta/"
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
    @GET("${API_VERSION}me/")
    fun getUserInfo(): Single<Response<User>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION" + "me/drive/root/children")
    fun getFiles(): Single<Response<DriveItemCloudTree>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("$API_VERSION" + "me/drive/items/{item_id}/children")
    fun getChildren(@Path(value = "item_id") id: String): Single<Response<DriveItemCloudTree>>

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

}