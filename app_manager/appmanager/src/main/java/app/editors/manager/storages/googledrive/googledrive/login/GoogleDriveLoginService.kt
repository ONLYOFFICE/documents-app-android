package app.editors.manager.storages.googledrive.googledrive.login

import app.documents.core.network.ApiContract
import app.editors.manager.storages.googledrive.mvp.models.resonse.UserResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GoogleDriveLoginService {

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + "application/x-www-form-urlencoded",
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @FormUrlEncoded
    @POST("o/oauth2/token")
    fun getToken(@FieldMap map: Map<String, String>): Single<Response<ResponseBody>>


    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/about/")
    fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String, @QueryMap map: Map<String, String> = mapOf("fields" to "user")): Single<Response<UserResponse>>

}
