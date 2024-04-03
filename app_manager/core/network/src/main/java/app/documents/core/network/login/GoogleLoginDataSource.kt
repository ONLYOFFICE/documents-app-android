package app.documents.core.network.login

import app.documents.core.model.login.GoogleUser
import app.documents.core.model.login.response.GoogleUserResponse
import app.documents.core.model.login.response.TokenResponse
import app.documents.core.network.ARG_CLIENT_ID
import app.documents.core.network.ARG_CLIENT_SECRET
import app.documents.core.network.ARG_CODE
import app.documents.core.network.ARG_GRANT_TYPE
import app.documents.core.network.ARG_REDIRECT_URI
import app.documents.core.network.ARG_REFRESH_TOKEN
import app.documents.core.network.BuildConfig
import app.documents.core.network.GOOGLE_DRIVE_AUTH_URL
import app.documents.core.network.GOOGLE_DRIVE_BASE_URL
import app.documents.core.network.HEADER_ACCEPT
import app.documents.core.network.HEADER_AUTHORIZATION
import app.documents.core.network.HEADER_CONTENT_TYPE
import app.documents.core.network.VALUE_ACCEPT
import app.documents.core.network.VALUE_CONTENT_TYPE
import app.documents.core.network.VALUE_GRANT_TYPE_AUTH
import app.documents.core.network.VALUE_GRANT_TYPE_REFRESH
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface GoogleLoginDataSource : StorageLoginDataSource<GoogleUser>

private interface GoogleLoginApi {

    @Headers(
        "$HEADER_CONTENT_TYPE: application/x-www-form-urlencoded",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @FormUrlEncoded
    @POST("/token")
    suspend fun signIn(@FieldMap map: Map<String, String>): TokenResponse

    @Headers(
        "$HEADER_CONTENT_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @GET
    suspend fun getUserInfo(
        @Url url: String,
        @Header(HEADER_AUTHORIZATION) token: String,
        @QueryMap map: Map<String, String> = mapOf("fields" to "user")
    ): GoogleUserResponse
}

internal class GoogleLoginDataSourceImpl(
    json: Json,
    okHttpClient: OkHttpClient
) : GoogleLoginDataSource {

    private val api: GoogleLoginApi = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(MediaType.get(VALUE_CONTENT_TYPE)))
        .baseUrl(GOOGLE_DRIVE_AUTH_URL)
        .build()
        .create(GoogleLoginApi::class.java)

    override suspend fun signIn(code: String): TokenResponse {
        val params = mapOf(
            ARG_CODE to code,
            ARG_CLIENT_ID to BuildConfig.GOOGLE_COM_CLIENT_ID,
            ARG_CLIENT_SECRET to BuildConfig.GOOGLE_COM_CLIENT_SECRET,
            ARG_REDIRECT_URI to BuildConfig.GOOGLE_COM_REDIRECT_URL,
            ARG_GRANT_TYPE to VALUE_GRANT_TYPE_AUTH,
        )
        return api.signIn(params)
    }

    override suspend fun refreshToken(refreshToken: String): TokenResponse {
        val params = mapOf(
            ARG_CLIENT_ID to BuildConfig.GOOGLE_COM_CLIENT_ID,
            ARG_CLIENT_SECRET to BuildConfig.GOOGLE_COM_CLIENT_SECRET,
            ARG_GRANT_TYPE to VALUE_GRANT_TYPE_REFRESH,
            ARG_REFRESH_TOKEN to refreshToken,
        )
        return api.signIn(params)
    }

    override suspend fun getUserInfo(accessToken: String): GoogleUser {
        val url = "$GOOGLE_DRIVE_BASE_URL/drive/v3/about/"
        return api.getUserInfo(url, "Bearer $accessToken").user
    }
}