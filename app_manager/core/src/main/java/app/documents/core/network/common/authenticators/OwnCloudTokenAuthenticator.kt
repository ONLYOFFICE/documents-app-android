package app.documents.core.network.common.authenticators

import app.documents.core.account.AccountRepository
import app.documents.core.model.login.OidcConfiguration
import app.documents.core.model.login.response.TokenResponse
import app.documents.core.network.login.owncloud.OwnCloudTokenDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route

class OwnCloudTokenAuthenticator(
    private val ownCloudTokenDataSource: OwnCloudTokenDataSource,
    private val accountRepository: AccountRepository,
    private val serverUrl: String
) : Authenticator {

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        synchronized(this) {
            val authHeader = response.request.header("Authorization")
            if (authHeader != null && authHeader.startsWith("Basic", ignoreCase = true)) {
                return null
            }

            val newTokens = refreshToken(ownCloudTokenDataSource.config)
            if (newTokens == null) return null
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .build()
        }
    }

    private fun refreshToken(currentConfig: OidcConfiguration?): TokenResponse? = runBlocking {
        try {
            val config = currentConfig ?: requireNotNull(
                ownCloudTokenDataSource.openidConfiguration(serverUrl)
            )

            val response = ownCloudTokenDataSource.refreshToken(
                url = config.tokenEndpoint,
                issuer = config.issuer,
                refreshToken = requireNotNull(accountRepository.getRefreshToken())
            )
            accountRepository.updateAccount(
                token = response.accessToken,
                refreshToken = response.refreshToken
            )
            response
        } catch (_: Throwable) {
            null
        }
    }
}
