package app.editors.manager.mvp.presenters.login

import android.accounts.Account
import android.content.Intent
import android.os.Build
import android.webkit.URLUtil
import app.documents.core.account.CloudAccount
import app.documents.core.login.LoginResponse
import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.Token
import app.documents.core.network.models.login.User
import app.documents.core.network.models.login.request.RequestSignIn
import app.documents.core.network.models.login.response.ResponseSignIn
import app.documents.core.network.models.login.response.ResponseUser
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.loginService
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.mvp.models.account.AccountsSqlData
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.base.BaseView
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.StringUtils.getUrlWithoutScheme
import lib.toolkit.base.managers.utils.StringUtils.isValidUrl
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

abstract class BaseLoginPresenter<View : BaseView> : BasePresenter<View>() {

    companion object {
        protected const val KEY_NULL_VALUE = "null"
    }

    protected var account: Account? = null
    private var disposable: Disposable? = null
    private var goggleJob: Job? = null

    override fun cancelRequest() {
        super.cancelRequest()
        disposable?.dispose()
    }

    /*
     * Common sign in
     * */
    protected fun signIn(requestSignIn: RequestSignIn) {
        disposable = context.loginService
            .signIn(requestSignIn)
            .subscribe({ loginResponse ->
                when (loginResponse) {
                    is LoginResponse.Success -> signInSuccess(
                        requestSignIn,
                        (loginResponse.response as ResponseSignIn).response
                    )
                    is LoginResponse.Error -> fetchError(loginResponse.error)
                }
            }, { fetchError(it) })
    }

    protected fun signInSuccess(requestSignIn: RequestSignIn, token: Token) {
        when {
            token.tfa == true -> onTwoFactorAuthApp(token.tfaKey, requestSignIn)
            token.sms == true -> onTwoFactorAuth(token.phoneNoise, requestSignIn)
            !token.token.isNullOrEmpty() -> getUserInfo(requestSignIn, token)
        }
    }

    protected fun getUserInfo(request: RequestSignIn, token: Token) {
        disposable = context.loginService.getUserInfo(token.token ?: "")
            .subscribe({ response ->
                when (response) {
                    is LoginResponse.Success -> createAccount(
                        (response.response as ResponseUser).response,
                        request,
                        token
                    )
                    is LoginResponse.Error -> fetchError(response.error)
                }
            }, { fetchError(it) })
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun createAccount(user: User, request: RequestSignIn, token: Token) {
        val portal = networkSettings.getPortal()
        val login = request.userName
        var password = request.password
        val provider = request.provider
        val accessToken = request.accessToken

        if (password.isEmpty() && provider.isNotEmpty() && accessToken.isNotEmpty()) {
            password = accessToken
        }

        val account = Account("$login@$portal", context.getString(lib.toolkit.base.R.string.account_type))

        val accountData = AccountData(
            portal = networkSettings.getPortal(),
            scheme = networkSettings.getScheme(),
            displayName = user.displayName,
            userId = user.id,
            provider = provider,
            accessToken = accessToken,
            email = login,
            avatar = user.avatarMedium,
            expires = token.expires
        )

        if (!AccountUtils.addAccount(context, account, password, accountData)) {
            AccountUtils.setAccountData(context, account, accountData)
            AccountUtils.setPassword(context, account, password)
        }
        AccountUtils.setToken(context, account, token.token)

        val accountDao = App.getApp().appComponent.accountsDao
        CoroutineScope(Dispatchers.IO).launch {
            accountDao.getAccountOnline()?.let {
                accountDao.updateAccount(it.copy(isOnline = false))
            }
            val newAccount = CloudAccount(
                id = user.id,
                login = login,
                portal = portal,
                scheme = networkSettings.getScheme(),
                name = user.displayName,
                provider = provider,
                avatarUrl = user.avatarMedium,
                serverVersion = networkSettings.serverVersion,
                isSslCiphers = networkSettings.getCipher(),
                isSslState = networkSettings.getSslState(),
                isOnline = true,
                isAdmin = user.isAdmin,
                isVisitor = user.isVisitor
            )
            accountDao.addAccount(newAccount)
            withContext(Dispatchers.Main) {
                onAccountCreateSuccess(newAccount)
            }
        }
    }

    protected open fun onTwoFactorAuth(phoneNoise: String?, request: RequestSignIn) {

    }

    protected open fun onTwoFactorAuthApp(secretKey: String?, request: RequestSignIn) {

    }

    /*
     * Get user config
     * */

    protected open fun onAccountCreateSuccess(account: CloudAccount) {
        FirebaseUtils.addAnalyticsLogin(account.portal ?: "", account.provider)
    }

    protected open fun onGetUser(user: app.editors.manager.mvp.models.user.User) {
    }

    override fun onDestroy() {
        disposable?.dispose()
        goggleJob?.cancel()
    }

    /*
     * Socials
     * */
    fun signInWithTwitter(token: String) {
        signIn(RequestSignIn(provider = ApiContract.Social.TWITTER, accessToken = token))
    }

    fun signInWithFacebook(token: String) {
        signIn(RequestSignIn(provider = ApiContract.Social.FACEBOOK, accessToken = token))
    }

    fun retrySignInWithGoogle() {
        signInWithGoogle(account)
    }

    fun signInWithGoogle(account: Account?) {
        if (goggleJob != null && goggleJob?.isActive == true) {
            return
        }
        this.account = account
        goggleJob = CoroutineScope(Dispatchers.IO).launch {
            val scope = context.getString(R.string.google_scope)
            try {
                val accessToken = GoogleAuthUtil.getToken(context, account, scope)
                withContext(Dispatchers.Main) {
                    signIn(
                        RequestSignIn(
                            userName = account?.name ?: "",
                            accessToken = accessToken,
                            provider = ApiContract.Social.GOOGLE
                        )
                    )
                }
            } catch (e: UserRecoverableAuthException) {
                withContext(Dispatchers.Main) {
                    onGooglePermission(e.intent)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    viewState.onError(e.message)
                }
            }

        }

    }

    /*
     * If we need user confirmation
     * */
    protected open fun onGooglePermission(intent: Intent) {}

    protected open fun isConfigConnection(t: Throwable?): Boolean {
        if (t is SSLHandshakeException && !networkSettings.getCipher() && networkSettings.getScheme() == ApiContract.SCHEME_HTTPS && Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            networkSettings.setCipher(true)
            return true
        } else if ((t is ConnectException || t is SocketTimeoutException || t is SSLHandshakeException ||
                    t is SSLPeerUnverifiedException) && networkSettings.getScheme() == ApiContract.SCHEME_HTTPS
        ) {
            networkSettings.setCipher(false)
            networkSettings.setScheme(ApiContract.SCHEME_HTTP)
            return true
        }
        return false
    }

    protected fun getPortal(url: String): String? {
        if (isValidUrl(url)) {
            networkSettings.setScheme(if (URLUtil.isHttpsUrl(url)) ApiContract.SCHEME_HTTPS else ApiContract.SCHEME_HTTP)
            return getUrlWithoutScheme(url)
        } else {
            val concatUrl = networkSettings.getScheme() + url
            if (isValidUrl(concatUrl)) {
                return url
            }
        }
        return null
    }
}