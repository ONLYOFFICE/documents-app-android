package app.documents.core.network.login.owncloud

import app.documents.core.model.login.OidcConfiguration
import app.documents.core.model.login.response.OwnCloudUserResponse
import app.documents.core.model.login.response.TokenResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

interface OwnCloudLoginDataSource : OwnCloudConfigDataSource {
    suspend fun signIn(url: String, issuer: String, code: String): TokenResponse
    suspend fun getUserInfo(url: String, accessToken: String): OwnCloudUserResponse
}

class OwnCloudLoginDataSourceImpl(json: Json, okHttpClient: OkHttpClient) :
    BaseOwnCloudDataSource(json, okHttpClient), OwnCloudLoginDataSource {

    override suspend fun signIn(url: String, issuer: String, code: String): TokenResponse {
        val params = getFieldsMap(code, issuer, isRefresh = false)
        return api.getToken(url, params)
    }

    override suspend fun getUserInfo(url: String, accessToken: String): OwnCloudUserResponse {
        return api.getUserInfo(url, "Bearer $accessToken")
    }

    override suspend fun openidConfiguration(serverUrl: String): OidcConfiguration? {
        return getConfiguration(serverUrl)
    }
}