package app.editors.manager.storages.dropbox.dropbox.login

import app.documents.core.network.ApiContract
import app.editors.manager.storages.dropbox.mvp.models.request.AccountRequest
import app.editors.manager.storages.dropbox.mvp.models.response.RefreshTokenResponse
import app.editors.manager.storages.dropbox.mvp.models.response.TokenResponse
import app.editors.manager.storages.dropbox.mvp.models.response.UserResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface DropboxLoginService {

    companion object {
        const val API_VERSION = "2/"
    }

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
    )
    @POST("oauth2/token")
    fun getRefreshToken(@Header("Authorization") auth: String, @QueryMap map: Map<String, String>): Single<Response<TokenResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
    )
    @POST("oauth2/token")
    fun updateRefreshToken(@Header("Authorization") auth: String, @QueryMap map: Map<String, String>): Single<Response<RefreshTokenResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}users/get_account")
    fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String, @Body map: AccountRequest): Single<Response<UserResponse>>

}