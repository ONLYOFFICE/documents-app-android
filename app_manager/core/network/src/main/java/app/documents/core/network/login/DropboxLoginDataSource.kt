package app.documents.core.network.login

import app.documents.core.model.login.response.DropboxUserResponse
import app.documents.core.model.login.response.RefreshTokenResponse
import app.documents.core.model.login.response.TokenResponse
import app.documents.core.network.ARG_CLIENT_ID
import app.documents.core.network.ARG_CLIENT_SECRET
import app.documents.core.network.ARG_CODE
import app.documents.core.network.ARG_GRANT_TYPE
import app.documents.core.network.ARG_REDIRECT_URI
import app.documents.core.network.ARG_REFRESH_TOKEN
import app.documents.core.network.BuildConfig
import app.documents.core.network.DROPBOX_BASE_URL
import app.documents.core.network.HEADER_AUTHORIZATION
import app.documents.core.network.HEADER_CONTENT_OPERATION_TYPE
import app.documents.core.network.VALUE_CONTENT_TYPE
import app.documents.core.network.VALUE_GRANT_TYPE_AUTH
import app.documents.core.network.VALUE_GRANT_TYPE_REFRESH
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface DropboxLoginDataSource {

    suspend fun signIn(code: String): TokenResponse

    suspend fun refreshToken(refreshToken: String): RefreshTokenResponse

    suspend fun getUserInfo(accessToken: String): DropboxUserResponse
}

private interface DropboxLoginApi {

    companion object {

        private const val API_VERSION = "2/"
    }

    @Headers("$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE")
    @POST("oauth2/token")
    suspend fun signIn(@QueryMap params: Map<String, String>): TokenResponse

    @POST("oauth2/token")
    suspend fun refreshToken(
        @Header(HEADER_AUTHORIZATION) credentials: String,
        @QueryMap map: Map<String, String>
    ): RefreshTokenResponse

    @Headers("$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE")
    @POST("${API_VERSION}users/get_current_account")
    suspend fun getUserInfo(@Header(HEADER_AUTHORIZATION) accessToken: String): DropboxUserResponse

}

internal class DropboxLoginDataSourceImpl(json: Json, okHttpClient: OkHttpClient) : DropboxLoginDataSource {

    private val api: DropboxLoginApi = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(DROPBOX_BASE_URL)
        .addConverterFactory(json.asConverterFactory(MediaType.get(VALUE_CONTENT_TYPE)))
        .build()
        .create(DropboxLoginApi::class.java)

    override suspend fun signIn(code: String): TokenResponse {
        val params = mapOf(
            ARG_CODE to code,
            ARG_GRANT_TYPE to VALUE_GRANT_TYPE_AUTH,
            ARG_REDIRECT_URI to BuildConfig.DROP_BOX_COM_REDIRECT_URL,
            ARG_CLIENT_ID to BuildConfig.DROP_BOX_COM_CLIENT_ID,
            ARG_CLIENT_SECRET to BuildConfig.DROP_BOX_COM_CLIENT_SECRET
        )
        return api.signIn(params)
    }

    override suspend fun getUserInfo(accessToken: String): DropboxUserResponse {
        return api.getUserInfo("Bearer $accessToken")
    }

    override suspend fun refreshToken(refreshToken: String): RefreshTokenResponse {
        val credentials = Credentials.basic(BuildConfig.DROP_BOX_COM_CLIENT_ID, BuildConfig.DROP_BOX_COM_CLIENT_SECRET)
        val params = mapOf(
            ARG_GRANT_TYPE to VALUE_GRANT_TYPE_REFRESH,
            ARG_REFRESH_TOKEN to refreshToken
        )
        return api.refreshToken(credentials, params)
    }

}