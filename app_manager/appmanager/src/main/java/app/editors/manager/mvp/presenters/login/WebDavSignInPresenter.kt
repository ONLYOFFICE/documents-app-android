package app.editors.manager.mvp.presenters.login

import android.accounts.Account
import android.net.Uri
import app.documents.core.storage.account.CloudAccount
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.webdav.WebDavService
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.webDavApi
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.login.WebDavSignInView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils
import moxy.InjectViewState
import moxy.presenterScope
import okhttp3.Credentials
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.URL

@InjectViewState
class WebDavSignInPresenter : BasePresenter<WebDavSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun cancelRequest() {
        super.cancelRequest()
        disposable?.dispose()
    }

    fun checkPortal(provider: WebDavService.Providers, url: String, login: String, password: String) {
        val builder = StringBuilder()
        if (url.contains(ApiContract.SCHEME_HTTPS) || url.contains(ApiContract.SCHEME_HTTP)) {
            builder.append(url)
        } else {
            builder.append(ApiContract.SCHEME_HTTPS).append(url)
        }

        try {
            var webUrl = URL(builder.toString())
            if (webUrl.path.isEmpty()) {
                if (provider == WebDavService.Providers.OwnCloud || provider == WebDavService.Providers.WebDav) {
                    builder.append(getPortalPath(webUrl.toString(), provider, login))
                } else {
                    builder.append(provider.path)
                }
            } else if (provider == WebDavService.Providers.OwnCloud) {
                builder.append(getPortalPath(webUrl.protocol + "://" + webUrl.host, provider, login))
            }
            webUrl = URL(builder.toString())

            networkSettings.setDefault()
            networkSettings.setScheme(webUrl.protocol + "://")
            networkSettings.setBaseUrl(webUrl.protocol + "://" + webUrl.host + if (webUrl.port == -1) "" else ":" + webUrl.port)

            viewState.onDialogWaiting(context.getString(R.string.dialogs_wait_title))
            disposable = context.webDavApi
                .capabilities(Credentials.basic(login, password), webUrl.path)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    checkResponse(response, provider, webUrl, login, password)
                }, { error ->
                    if (error is ConnectException && builder.toString().startsWith(ApiContract.SCHEME_HTTPS)) {
                        val httpUrl = builder.toString().replace(ApiContract.SCHEME_HTTPS.toRegex(), ApiContract.SCHEME_HTTP)
                        checkPortal(provider, httpUrl, login, password)
                    } else {
                        viewState.onDialogClose()
                        fetchError(error)
                    }
                })
        } catch (error: MalformedURLException) {
            viewState.onError(context.getString(R.string.errors_path_url))
        }
    }

    private fun checkResponse(
        response: Response<ResponseBody>,
        provider: WebDavService.Providers,
        webUrl: URL,
        login: String,
        password: String
    ) {
        val url = webUrl.toString()
        if (response.isSuccessful && response.code() == 207) {
            createUser(provider, webUrl, login, password)
        } else if (response.code() == 404 && url.startsWith(ApiContract.SCHEME_HTTPS)) {
            val httpUrl =
                url.replace(ApiContract.SCHEME_HTTPS.toRegex(), ApiContract.SCHEME_HTTP)
            checkPortal(provider, httpUrl, login, password)
        } else {
            viewState.onDialogClose()
            fetchError(HttpException(response))
        }
    }

    fun checkNextCloud(provider: WebDavService.Providers, url: String) {
        val builder = StringBuilder()
        if (url.contains(ApiContract.SCHEME_HTTPS) || url.contains(ApiContract.SCHEME_HTTP)) {
            builder.append(url)
        } else {
            builder.append(ApiContract.SCHEME_HTTPS).append(url)
        }
        try {
            val correctUrl = URL(builder.toString())

            val path = correctUrl.path.removeSuffix("/").removePrefix("/")

            networkSettings.setDefault()
            networkSettings.setBaseUrl(correctUrl.protocol + "://" + correctUrl.host + if (correctUrl.port != -1 ) ":${correctUrl.port}" else "" + "/")
            networkSettings.setScheme(correctUrl.protocol + "://")

            viewState.onDialogWaiting(context.getString(R.string.dialogs_check_portal_header_text))
            disposable = context.webDavApi
                .capability("$path/index.php/login/flow")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response.isSuccessful && response.code() == 200 || response.code() == 401) {
                        viewState.onNextCloudLogin(correctUrl.toString())
                    } else {
                        if (correctUrl.toString().startsWith(ApiContract.SCHEME_HTTPS)) {
                            val httpUrl = correctUrl.toString()
                                .replace(ApiContract.SCHEME_HTTPS.toRegex(), ApiContract.SCHEME_HTTP)
                            checkNextCloud(provider, httpUrl)
                        } else {
                            onErrorHandle(response.body(), response.code())
                            viewState.onUrlError(context.getString(R.string.errors_path_url))
                        }
                    }
                }, { error ->
                    if (correctUrl.toString().startsWith(ApiContract.SCHEME_HTTPS)) {
                        val httpUrl =
                            correctUrl.toString().replace(ApiContract.SCHEME_HTTPS.toRegex(), ApiContract.SCHEME_HTTP)
                        checkNextCloud(provider, httpUrl)
                    } else {
                        onFailureHandle(error)
                    }
                })
        } catch (e: MalformedURLException) {
            val message = e.message
            if (message != null && message.contains("Invalid URL")) {
                viewState.onDialogClose()
                viewState.onUrlError(context.getString(R.string.errors_path_url))
            }
        }
    }

    private fun createUser(provider: WebDavService.Providers, webUrl: URL, login: String, password: String) {
        val cloudAccount = CloudAccount(
            id = "$login@${webUrl.host}",
            isWebDav = true,
            portal = webUrl.host + if (webUrl.port != -1 ) ":${webUrl.port}" else "",
            webDavPath = webUrl.path,
            webDavProvider = provider.name,
            login = login,
            scheme = webUrl.protocol + "://",
            isSslState = networkSettings.getSslState(),
            isSslCiphers = networkSettings.getCipher(),
            name = login
        )

        val accountData = AccountData(
            portal = cloudAccount.portal + if (webUrl.port != -1 ) ":${webUrl.port}" else "",
            scheme = cloudAccount.scheme ?: "",
            displayName = login,
            userId = cloudAccount.id,
            provider = cloudAccount.webDavProvider ?: "",
            webDav = cloudAccount.webDavPath,
            email = login,
        )

        val account = Account(cloudAccount.getAccountName(), context.getString(lib.toolkit.base.R.string.account_type))

        if (AccountUtils.addAccount(context, account, password, accountData)) {
            addAccountToDb(cloudAccount)
        } else {
            AccountUtils.setAccountData(context, account, accountData)
            AccountUtils.setPassword(context, account, password)
            addAccountToDb(cloudAccount)
        }
    }

    private fun addAccountToDb(cloudAccount: CloudAccount) {
        presenterScope.launch {
            accountDao.getAccountOnline()?.let {
                accountDao.addAccount(it.copy(isOnline = false))
            }
            accountDao.addAccount(cloudAccount.copy(isOnline = true))
            withContext(Dispatchers.Main) {
                viewState.onLogin()
            }
        }
    }

    private fun getPortalPath(url: String, provider: WebDavService.Providers, login: String): String {
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
            if (provider != WebDavService.Providers.WebDav) {
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
            if (provider == WebDavService.Providers.WebDav) {
                provider.path
            } else {
                provider.path + login
            }
        }
    }
}