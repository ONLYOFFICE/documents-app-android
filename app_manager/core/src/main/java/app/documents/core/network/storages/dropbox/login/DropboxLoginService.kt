package app.documents.core.network.storages.dropbox.login

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.storages.dropbox.models.request.AccountRequest
import app.documents.core.network.storages.dropbox.models.response.RefreshTokenResponse
import app.documents.core.network.storages.dropbox.models.response.TokenResponse
import app.documents.core.network.storages.dropbox.models.response.UserResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface DropboxLoginService {

    companion object {
        const val API_VERSION = "2/"
    }

    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,)
    @POST("oauth2/token")
    fun getRefreshToken(
        @Header("Authorization") auth: String,
        @QueryMap map: Map<String, String>
    ): Single<Response<TokenResponse>>

    @POST("oauth2/token")
    fun updateRefreshToken(
        @Header("Authorization") auth: String,
        @QueryMap map: Map<String, String>
    ): Single<Response<RefreshTokenResponse>>

    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("${API_VERSION}users/get_account")
    fun getUserInfo(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @Body map: AccountRequest
    ): Single<Response<UserResponse>>

}