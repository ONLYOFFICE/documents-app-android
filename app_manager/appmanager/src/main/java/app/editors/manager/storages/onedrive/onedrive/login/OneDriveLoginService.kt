package app.editors.manager.storages.onedrive.onedrive.login

import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.storages.onedrive.mvp.models.response.AuthResponse
import app.editors.manager.storages.onedrive.mvp.models.user.User
import app.editors.manager.storages.onedrive.onedrive.api.OneDriveService
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface OneDriveLoginService {

    companion object {
        const val API_VERSION = "beta/"
        const val ONEDRIVE_BASE_URL = "https://graph.microsoft.com/"
    }
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("${OneDriveService.API_VERSION}me/")
    fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Single<Response<User>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + "application/x-www-form-urlencoded",
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @FormUrlEncoded
    @POST("common/oauth2/v2.0/token")
    fun getToken(@FieldMap request: Map<String, String>): Single<Response<AuthResponse>>
}