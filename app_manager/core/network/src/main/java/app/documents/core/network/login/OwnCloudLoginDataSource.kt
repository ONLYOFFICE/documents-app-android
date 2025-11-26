package app.documents.core.network.login

import app.documents.core.model.login.OidcConfiguration
import app.documents.core.model.login.response.OwnCloudUserResponse
import app.documents.core.model.login.response.TokenResponse
import app.documents.core.network.ARG_CLIENT_ID
import app.documents.core.network.ARG_CLIENT_SECRET
import app.documents.core.network.ARG_CODE
import app.documents.core.network.ARG_GRANT_TYPE
import app.documents.core.network.ARG_REDIRECT_URI
import app.documents.core.network.ARG_REFRESH_TOKEN
import app.documents.core.network.HEADER_AUTHORIZATION
import app.documents.core.network.HTTP_METHOD_POST
import app.documents.core.network.OWNCLOUD_CLIENT_ID
import app.documents.core.network.OWNCLOUD_CLIENT_SECRET
import app.documents.core.network.VALUE_CONTENT_TYPE
import app.documents.core.network.VALUE_GRANT_TYPE_AUTH
import app.documents.core.network.VALUE_GRANT_TYPE_REFRESH
import app.documents.core.network.OWNCLOUD_REDIRECT_SUFFIX
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Url

private interface OwnCloudApi {
    @GET
    suspend fun getUserInfo(
        @Url url: String,
        @Header(HEADER_AUTHORIZATION) accessToken: String
    ): OwnCloudUserResponse


    @FormUrlEncoded
    @HTTP(method = HTTP_METHOD_POST, hasBody = true)
    suspend fun getToken(
        @Url url: String,
        @FieldMap fields: Map<String, String>
    ): TokenResponse

    @GET
    suspend fun getOpenidConfiguration(@Url url: String): Response<ResponseBody>
}

interface OwnCloudLoginDataSource {
    suspend fun signIn(url: String, issuer: String, code: String): TokenResponse
    suspend fun refreshToken(url: String, issuer: String, refreshToken: String): TokenResponse
    suspend fun getUserInfo(url: String, accessToken: String): OwnCloudUserResponse
    suspend fun openidConfiguration(serverUrl: String): OidcConfiguration?
}

internal class OwnCloudLoginDataSourceImpl(json: Json, okHttpClient: OkHttpClient) :
    OwnCloudLoginDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    private val api: OwnCloudApi = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("http://stub")
        .addConverterFactory(json.asConverterFactory(MediaType.get(VALUE_CONTENT_TYPE)))
        .build()
        .create(OwnCloudApi::class.java)

    override suspend fun signIn(url: String, issuer: String, code: String): TokenResponse {
        val params = getFieldsMap(code, issuer, isRefresh = false)
        return api.getToken(url, params)
    }

    override suspend fun refreshToken(
        url: String,
        issuer: String,
        refreshToken: String
    ): TokenResponse {
        val params = getFieldsMap(refreshToken, issuer, isRefresh = true)
        return api.getToken(url, params)
    }

    override suspend fun getUserInfo(url: String, accessToken: String): OwnCloudUserResponse {
        return api.getUserInfo(url, "Bearer $accessToken")
    }


    override suspend fun openidConfiguration(serverUrl: String): OidcConfiguration? {
        val wellKnownUrl = serverUrl.removeSuffix("/") + "/.well-known/openid-configuration"
        val response = api.getOpenidConfiguration(wellKnownUrl)
        if (!response.isSuccessful) throw HttpException(response)

        return try {
            json.decodeFromString(response.body()?.string().orEmpty())
        } catch (_: Throwable) {
            null
        }
    }

    private fun getFieldsMap(data: String, issuer: String, isRefresh: Boolean): Map<String, String> {
        val dataKey = if (isRefresh) ARG_REFRESH_TOKEN else ARG_CODE
        return mapOf(
            dataKey to data,
            ARG_GRANT_TYPE to if (isRefresh) VALUE_GRANT_TYPE_REFRESH else VALUE_GRANT_TYPE_AUTH,
            ARG_REDIRECT_URI to issuer + OWNCLOUD_REDIRECT_SUFFIX,
            ARG_CLIENT_ID to OWNCLOUD_CLIENT_ID,
            ARG_CLIENT_SECRET to OWNCLOUD_CLIENT_SECRET
        )
    }
}