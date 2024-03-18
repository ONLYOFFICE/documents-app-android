package app.documents.core.network.login

import app.documents.core.model.cloud.Scheme
import app.documents.core.model.login.response.OnedriveSignInResponse
import app.documents.core.model.login.response.OnedriveUser
import app.documents.core.network.ARG_CLIENT_ID
import app.documents.core.network.ARG_CLIENT_SECRET
import app.documents.core.network.ARG_CODE
import app.documents.core.network.ARG_GRANT_TYPE
import app.documents.core.network.ARG_REDIRECT_URI
import app.documents.core.network.ARG_REFRESH_TOKEN
import app.documents.core.network.ARG_SCOPE
import app.documents.core.network.BuildConfig
import app.documents.core.network.HEADER_ACCEPT
import app.documents.core.network.HEADER_AUTHORIZATION
import app.documents.core.network.HEADER_CONTENT_OPERATION_TYPE
import app.documents.core.network.ONEDRIVE_API_VERSION
import app.documents.core.network.ONEDRIVE_AUTH_URL
import app.documents.core.network.ONEDRIVE_PORTAL_URL
import app.documents.core.network.ONEDRIVE_VALUE_SCOPE
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
import retrofit2.http.Url

interface OnedriveLoginDataSource {

    suspend fun signIn(code: String): OnedriveSignInResponse

    suspend fun refreshToken(refreshToken: String): OnedriveSignInResponse

    suspend fun getUserInfo(accessToken: String): OnedriveUser
}

private interface OnedriveLoginApi {

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: application/x-www-form-urlencoded",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @FormUrlEncoded
    @POST("common/oauth2/v2.0/token")
    suspend fun signIn(@FieldMap request: Map<String, String>): OnedriveSignInResponse

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @GET
    suspend fun getUserInfo(@Url url: String, @Header(HEADER_AUTHORIZATION) token: String): OnedriveUser
}

internal class OnedriveLoginDataSourceImpl(json: Json, okHttpClient: OkHttpClient) : OnedriveLoginDataSource {

    private val api: OnedriveLoginApi = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(ONEDRIVE_AUTH_URL)
        .addConverterFactory(json.asConverterFactory(MediaType.get(VALUE_CONTENT_TYPE)))
        .build()
        .create(OnedriveLoginApi::class.java)

    private val commonParams = mapOf(
        ARG_CLIENT_ID to BuildConfig.ONE_DRIVE_COM_CLIENT_ID,
        ARG_CLIENT_SECRET to BuildConfig.ONE_DRIVE_COM_CLIENT_SECRET,
        ARG_REDIRECT_URI to BuildConfig.ONE_DRIVE_COM_REDIRECT_URL,
        ARG_SCOPE to ONEDRIVE_VALUE_SCOPE,
    )

    override suspend fun signIn(code: String): OnedriveSignInResponse {
        val params = mapOf(
            ARG_GRANT_TYPE to VALUE_GRANT_TYPE_AUTH,
            ARG_CODE to code
        )
        return api.signIn(commonParams.plus(params))
    }

    override suspend fun refreshToken(refreshToken: String): OnedriveSignInResponse {
        val params = mapOf(
            ARG_GRANT_TYPE to VALUE_GRANT_TYPE_REFRESH,
            ARG_REFRESH_TOKEN to refreshToken
        )
        return api.signIn(commonParams.plus(params))
    }

    override suspend fun getUserInfo(accessToken: String): OnedriveUser {
        val url = StringBuilder()
            .append(Scheme.Https.value)
            .append("$ONEDRIVE_PORTAL_URL/")
            .append("$ONEDRIVE_API_VERSION/")
            .append("me/")

        return api.getUserInfo(url.toString(), accessToken)
    }
}
