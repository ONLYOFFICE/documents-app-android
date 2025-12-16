package app.documents.core.network.login.owncloud

import app.documents.core.model.login.OidcConfiguration
import app.documents.core.model.login.response.TokenResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

interface OwnCloudTokenDataSource : OwnCloudConfigDataSource {
    val config: OidcConfiguration?
    suspend fun refreshToken(url: String, issuer: String, refreshToken: String): TokenResponse
}

class OwnCloudTokenDataSourceImpl(json: Json, okHttpClient: OkHttpClient) :
    BaseOwnCloudDataSource(json, okHttpClient), OwnCloudTokenDataSource {

    override var config: OidcConfiguration? = null

    override suspend fun refreshToken(
        url: String,
        issuer: String,
        refreshToken: String
    ): TokenResponse {
        val params = getFieldsMap(refreshToken, issuer, isRefresh = true)
        return api.getToken(url, params)
    }

    override suspend fun openidConfiguration(serverUrl: String): OidcConfiguration? {
        return try {
            config = getConfiguration(serverUrl)
            config
        } catch (_: Throwable) {
            null
        }
    }
}