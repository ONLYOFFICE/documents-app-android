package app.documents.core.login

import android.net.Uri
import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.login.WebdavLoginDataSource
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
    data class Error(val exception: Throwable) : WebdavLoginResult()
    data object Success : WebdavLoginResult()
}

interface WebdavLoginRepository {

    suspend fun checkPortal(
        provider: WebdavProvider,
        url: String,
        login: String,
        password: String
    ): Flow<WebdavLoginResult>
}

internal class WebdavLoginRepositoryImpl(
    private val accountRepository: AccountRepository,
    private val webdavLoginDataSource: WebdavLoginDataSource
) : WebdavLoginRepository {

    override suspend fun checkPortal(
        provider: WebdavProvider,
        url: String,
        login: String,
        password: String
    ): Flow<WebdavLoginResult> {
        return flow {
            if (provider is WebdavProvider.NextCloud) {
                try {
                    webdavLoginDataSource.capability(url)
                } catch (e: ConnectException) {
                    webdavLoginDataSource.capability(url.replace(Scheme.Https.value, Scheme.Http.value))
                }
                return@flow emit(WebdavLoginResult.NextCloudLogin(url))
            } else {
                var webStringUrl = buildWebStringUrl(url, provider, password)

                try {
                    webdavLoginDataSource.capabilities(webStringUrl, Credentials.basic(login, password))
                } catch (e: ConnectException) {
                    webStringUrl = webStringUrl.replace(Scheme.Https.value, Scheme.Http.value)
                    webdavLoginDataSource.capabilities(
                        webStringUrl,
                        Credentials.basic(login, password)
                    )
                }
                accountRepository.addAccount(createCloudAccount(login, URL(webStringUrl), provider), password)
            }
            return@flow emit(WebdavLoginResult.Success)
        }.flowOn(Dispatchers.IO)
            .catch { cause -> emit(WebdavLoginResult.Error(cause)) }
    }

    private fun createCloudAccount(login: String, webUrl: URL, provider: WebdavProvider): CloudAccount {
        val url = webUrl.host + if (webUrl.port != -1) ":${webUrl.port}" else ""
        return CloudAccount(
            id = UUID.nameUUIDFromBytes("$login@${webUrl.host}".toByteArray()).toString(),
            portalUrl = url,
            login = login,
            name = login,
            portal = CloudPortal(
                url = url,
                scheme = Scheme.Custom("${webUrl.protocol}://"),
                provider = PortalProvider.Webdav(provider)
            )
        )
    }

    private fun buildWebStringUrl(url: String, provider: WebdavProvider, login: String): String {
        val builder = StringBuilder().checkScheme(url)
        val webUrl = URL(builder.toString())

        if (webUrl.path.isEmpty()) {
            if (provider == WebdavProvider.OwnCloud || provider == WebdavProvider.WebDav) {
                builder.append(getPortalPath(webUrl.toString(), provider, login))
            } else {
                builder.append(provider.path)
            }
        } else if (provider == WebdavProvider.OwnCloud) {
            builder.append(getPortalPath(webUrl.protocol + "://" + webUrl.host, provider, login))
        }

        return builder.toString()
    }

    private fun StringBuilder.checkScheme(url: String): StringBuilder {
        return if (StringUtils.hasScheme(url)) {
            append(url)
        } else {
            append(ApiContract.SCHEME_HTTPS)
            append(url)
        }
    }

    private fun getPortalPath(url: String, provider: WebdavProvider, login: String): String {
        val uri = Uri.parse(url)
        var path = uri.path
        val base = uri.authority
        if (base != null && path?.contains(base) == true) {
            path = path.replace(base.toRegex(), "")
        }
        return if (path != null && path != "") {
            val builder = StringBuilder()
            if (path[path.length - 1] == '/') {
                path = path.substring(0, path.lastIndexOf('/'))
            }
            if (provider != WebdavProvider.WebDav) {
                builder.append(path)
                    .append(provider.path)
                    .append(login)
                    .toString()
            } else {
                builder.append(path)
                    .append(provider.path)
                    .toString()
            }
        } else {
            if (provider == WebdavProvider.WebDav) {
                provider.path
            } else {
                provider.path + login
            }
        }
    }
}