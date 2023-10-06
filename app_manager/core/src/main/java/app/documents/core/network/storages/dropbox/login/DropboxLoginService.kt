package app.documents.core.network.storages.dropbox.login

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.storages.dropbox.models.response.RefreshTokenResponse
import app.documents.core.network.storages.dropbox.models.response.TokenResponse
import app.documents.core.network.storages.dropbox.models.response.UserResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface DropboxLoginService {

    companion object {
        const val API_VERSION = "2/"
    }

    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("oauth2/token")
    suspend fun getAccessToken(@QueryMap params: Map<String, String>): Response<TokenResponse>

    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("oauth2/token")
    suspend fun getRefreshToken(
        @Header("Authorization") auth: String,
        @QueryMap map: Map<String, String>
    ): Response<TokenResponse>

    @POST("oauth2/token")
    suspend fun updateRefreshToken(
        @Header("Authorization") auth: String,
        @QueryMap map: Map<String, String>
    ): Response<RefreshTokenResponse>

    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("${API_VERSION}users/get_current_account")
    suspend fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Response<UserResponse>

}