package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.model.login.OidcConfiguration
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.login.WebdavLoginDataSource
import app.documents.core.network.login.owncloud.OwnCloudLoginDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.Credentials
import java.net.ConnectException
import java.net.URL
import java.util.UUID

sealed class WebdavLoginResult {

    data class NextCloudLogin(val url: String) : WebdavLoginResult()
    data class OwnCloudLogin(val config: OidcConfiguration?) : WebdavLoginResult()
    data class Error(val exception: Throwable) : WebdavLoginResult()
    data object Success : WebdavLoginResult()
}

interface WebdavLoginRepository {

    suspend fun signIn(
        provider: WebdavProvider,
        url: String,
        login: String,
        password: String
    ): Flow<WebdavLoginResult>
}

internal class WebdavLoginRepositoryImpl(
    private val accountRepository: AccountRepository,
    private val webdavLoginDataSource: WebdavLoginDataSource,
    private val ownCloudLoginDataSource: OwnCloudLoginDataSource
) : WebdavLoginRepository {

    override suspend fun signIn(
        provider: WebdavProvider,
        url: String,
        login: String,
        password: String
    ): Flow<WebdavLoginResult> {
        return flow {

            if (provider is WebdavProvider.OwnCloud && login.isEmpty()) {
                checkOwnCloud(url).apply {
                    return@flow emit(WebdavLoginResult.OwnCloudLogin(this))
                }
            }

            if (checkNextCloud(provider, url)) {
                return@flow emit(WebdavLoginResult.NextCloudLogin(url))
            }

            val webUrl = URL(checkCapabilities(url, login, password, provider))
            saveAccount(url, login, password, webUrl.protocol, provider, webUrl.path)

            return@flow emit(WebdavLoginResult.Success)
        }.flowOn(Dispatchers.IO)
            .catch { cause -> emit(WebdavLoginResult.Error(cause)) }
    }

    private suspend fun checkNextCloud(
        provider: WebdavProvider,
        url: String
    ): Boolean {
        if (provider is WebdavProvider.NextCloud) {
            return try {
                webdavLoginDataSource.capability(url)
                true
            } catch (_: ConnectException) {
                webdavLoginDataSource.capability(url.replace(Scheme.Https.value, Scheme.Http.value))
                true
            }
        }
        return false
    }

    private suspend fun checkOwnCloud(url: String): OidcConfiguration? {
        return ownCloudLoginDataSource.openidConfiguration(url)
    }

    private suspend fun checkCapabilities(
        url: String,
        login: String,
        password: String,
        provider: WebdavProvider
    ): String {
        var webStringUrl = buildWebStringUrl(url, provider, login)
        try {
            webdavLoginDataSource.capabilities(webStringUrl, Credentials.basic(login, password))
        } catch (_: ConnectException) {
            webStringUrl = webStringUrl.replace(Scheme.Https.value, Scheme.Http.value)
            webdavLoginDataSource.capabilities(
                webStringUrl,
                Credentials.basic(login, password)
            )
        }
        return webStringUrl
    }

    private suspend fun saveAccount(
        url: String,
        login: String,
        password: String,
        scheme: String,
        provider: WebdavProvider,
        path: String
    ) {
        val portal = url.replace(""".*://""".toRegex(), "")
        accountRepository.addAccount(
            cloudAccount = CloudAccount(
                id = UUID.nameUUIDFromBytes("$login@$url".toByteArray()).toString(),
                portalUrl = portal,
                login = login,
                name = login,
                portal = CloudPortal(
                    url = portal,
                    scheme = Scheme.Custom("$scheme://"),
                    provider = PortalProvider.Webdav(provider, path)
                )
            ),
            password = password
        )
    }

    private fun buildWebStringUrl(url: String, provider: WebdavProvider, login: String): String {
        return StringBuilder()
            .apply {
                append(url)
                if (!StringUtils.hasScheme(url)) append(ApiContract.SCHEME_HTTPS)
                when (provider) {
                    WebdavProvider.OwnCloud -> {
                        append(WebdavProvider.DEFAULT_OWNCLOUD_PATH)
                        append("$login/")
                    }
                    else -> Unit
                }
            }.toString()
    }
}