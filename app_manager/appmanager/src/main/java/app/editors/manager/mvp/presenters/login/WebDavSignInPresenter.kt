package app.editors.manager.mvp.presenters.login

import android.accounts.Account
import android.net.Uri
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.webDavApi
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.login.WebDavSignInView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
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

    fun checkPortal(provider: WebdavProvider, url: String, login: String, password: String) {
        val builder = StringBuilder()
        if (url.contains(ApiContract.SCHEME_HTTPS) || url.contains(ApiContract.SCHEME_HTTP)) {
            builder.append(url)
        } else {
            builder.append(ApiContract.SCHEME_HTTPS).append(url)
        }

        try {
            var webUrl = URL(builder.toString())
            if (webUrl.path.isEmpty()) {
                if (provider == WebdavProvider.OwnCloud || provider == WebdavProvider.WebDav) {
                    builder.append(getPortalPath(webUrl.toString(), provider, login))
                } else {
                    builder.append(provider.path)
                }
            } else if (provider == WebdavProvider.OwnCloud) {
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
                        val httpUrl =
                            builder.toString().replace(ApiContract.SCHEME_HTTPS.toRegex(), ApiContract.SCHEME_HTTP)
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
        provider: WebdavProvider,
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

    fun checkNextCloud(provider: WebdavProvider, url: String) {
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
            networkSettings.setBaseUrl(correctUrl.protocol + "://" + correctUrl.host + if (correctUrl.port != -1) ":${correctUrl.port}" else "" + "/")
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

    private fun createUser(provider: WebdavProvider, webUrl: URL, login: String, password: String) {
        val cloudAccount = CloudAccount(
            id = "$login@${webUrl.host}",
            login = login,
            name = login,
            portal = CloudPortal(
                scheme = Scheme.Custom("${webUrl.protocol}://"),
                url = webUrl.host + if (webUrl.port != -1) ":${webUrl.port}" else "",
                provider = PortalProvider.Webdav(provider),
                settings = PortalSettings(
                    isSslState = networkSettings.getSslState(),
                    isSslCiphers = networkSettings.getCipher()
                )
            ),
        )

        val accountData = AccountData(
            portal = cloudAccount.portal.url + if (webUrl.port != -1) ":${webUrl.port}" else "",
            scheme = cloudAccount.portal.scheme.value,
            displayName = login,
            userId = cloudAccount.id,
            email = login,
        )

        val account = Account(cloudAccount.accountName, context.getString(lib.toolkit.base.R.string.account_type))

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
            // TODO: extract to repository
//            cloudDataSource.getAccountOnline()?.let {
//                cloudDataSource.addAccount(it.copy(isOnline = false))
//            }
//            cloudDataSource.addAccount(cloudAccount.copy(isOnline = true))
//            withContext(Dispatchers.Main) {
//                viewState.onLogin()
//            }
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