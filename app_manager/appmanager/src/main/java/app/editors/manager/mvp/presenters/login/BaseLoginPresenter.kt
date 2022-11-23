package app.editors.manager.mvp.presenters.login

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.util.Log
import android.webkit.URLUtil
import app.documents.core.storage.account.CloudAccount
import app.documents.core.storage.account.copyWithToken
import app.documents.core.network.login.LoginResponse
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.user.User
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.loginService
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.utils.GoogleUtils
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
import moxy.presenterScope
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

abstract class BaseLoginPresenter<View : BaseView> : BasePresenter<View>() {

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
    protected fun signIn(requestSignIn: app.documents.core.network.login.models.request.RequestSignIn) {
        disposable = context.loginService
            .signIn(requestSignIn)
            .subscribe({ loginResponse ->
                when (loginResponse) {
                    is LoginResponse.Success -> signInSuccess(
                        requestSignIn,
                        (loginResponse.response as app.documents.core.network.login.models.response.ResponseSignIn).response
                    )
                    is LoginResponse.Error -> fetchError(loginResponse.error)
                }
            }, { fetchError(it) })
    }

    protected fun signInSuccess(requestSignIn: app.documents.core.network.login.models.request.RequestSignIn, token: app.documents.core.network.login.models.Token) {
        when {
            !token.token.isNullOrEmpty() -> getUserInfo(requestSignIn, token)
            token.tfa == true -> onTwoFactorAuthApp(token.tfaKey, requestSignIn)
            token.sms == true -> onTwoFactorAuth(token.phoneNoise, requestSignIn)
        }
    }

    protected fun getUserInfo(request: app.documents.core.network.login.models.request.RequestSignIn, token: app.documents.core.network.login.models.Token) {
        disposable = context.loginService.getUserInfo(token.token ?: "")
            .subscribe({ response ->
                when (response) {
                    is LoginResponse.Success -> {
                        subscribePush((response.response as app.documents.core.network.login.models.response.ResponseUser).response, request, token)
                    }
                    is LoginResponse.Error -> fetchError(response.error)
                }
            }, { fetchError(it) })
    }

    private fun subscribePush(response: app.documents.core.network.login.models.User, request: app.documents.core.network.login.models.request.RequestSignIn, token: app.documents.core.network.login.models.Token) {
        GoogleUtils.getDeviceToken({ deviceToken ->
            disposable = context.loginService.setFirebaseToken(token.token ?: "", deviceToken)
                .flatMap { responseRegisterToken ->
                    if (responseRegisterToken.isSuccessful) {
                        return@flatMap context.loginService.subscribe(token.token ?: "", deviceToken, true)
                    } else {
                        throw RuntimeException("Error subscribe push")
                    }
                }.subscribe({ responseSubscribe ->
                    if (responseSubscribe.isSuccessful) {
                        preferenceTool.deviceMessageToken = deviceToken
                        createAccount(response, request, token)
                    }
                }) {
                    Log.e(TAG, "subscribePush: ${it.message}")
                    createAccount(response, request, token)
                }
        }) {
            Log.e(TAG, "subscribePush: ${it.message}")
            createAccount(response, request, token)
        }
    }

    @SuppressLint("CheckResult")
    protected fun unsubscribePush(account: CloudAccount, token: String? = null, result: ((error: Throwable?) -> Unit)? = null) {
        if (token == null || token.isEmpty()) {
            result?.invoke(null)
            return
        }
        GoogleUtils.getDeviceToken({ deviceToken ->
            context.loginService.subscribe(token, deviceToken, false)
                .subscribe({
                    if (it.isSuccessful) {
                        result?.invoke(null)
                    } else {
                        result?.invoke(RuntimeException("Error unsubscribe"))
                    }
                }) {
                    result?.invoke(RuntimeException("Error unsubscribe"))
                }
        }) {
            result?.invoke(RuntimeException("Error unsubscribe"))
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun createAccount(user: app.documents.core.network.login.models.User, request: app.documents.core.network.login.models.request.RequestSignIn, token: app.documents.core.network.login.models.Token) {
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
            displayName = user.getName(),
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
        presenterScope.launch {
            accountDao.getAccountOnline()?.let {
                unsubscribePush(it)
                accountDao.updateAccount(it.copyWithToken(isOnline = false))
            }
            val newAccount = CloudAccount(
                id = user.id,
                login = login,
                portal = portal,
                scheme = networkSettings.getScheme(),
                name = user.getName(),
                provider = provider,
                avatarUrl = user.avatarMedium,
                serverVersion = networkSettings.serverVersion,
                isSslCiphers = networkSettings.getCipher(),
                isSslState = networkSettings.getSslState(),
                isOnline = true,
                isAdmin = user.isAdmin,
                isVisitor = user.isVisitor
            ).apply {
                setCryptToken(token.token ?: "")
                setCryptPassword(password)
                this.expires = token.expires ?: ""
            }
            accountDao.addAccount(newAccount)
            withContext(Dispatchers.Main) {
                onAccountCreateSuccess(newAccount)
            }
        }
    }

    protected open fun onTwoFactorAuth(phoneNoise: String?, request: app.documents.core.network.login.models.request.RequestSignIn) {
        preferenceTool.phoneNoise = phoneNoise
    }

    protected open fun onTwoFactorAuthApp(secretKey: String?, request: app.documents.core.network.login.models.request.RequestSignIn) {

    }

    /*
     * Get user config
     * */

    protected open fun onAccountCreateSuccess(account: CloudAccount) {
        FirebaseUtils.addAnalyticsLogin(account.portal ?: "", account.provider)
    }

    protected open fun onGetUser(user: User) {
    }

    override fun onDestroy() {
        disposable?.dispose()
        goggleJob?.cancel()
    }

    /*
     * Socials
     * */
    fun signInWithTwitter(token: String) {
        signIn(
            app.documents.core.network.login.models.request.RequestSignIn(
                provider = ApiContract.Social.TWITTER,
                accessToken = token
            )
        )
    }

    fun signInWithFacebook(token: String) {
        signIn(
            app.documents.core.network.login.models.request.RequestSignIn(
                provider = ApiContract.Social.FACEBOOK,
                accessToken = token
            )
        )
    }

    fun retrySignInWithGoogle() {
        signInWithGoogle(account)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun signInWithGoogle(account: Account?) {
        if (goggleJob != null && goggleJob?.isActive == true) {
            return
        }
        this.account = account
        goggleJob = presenterScope.launch((Dispatchers.IO)) {
            val scope = context.getString(R.string.google_scope)
            try {
                if (account != null) {
                    val accessToken = GoogleAuthUtil.getToken(context, account, scope)
                    withContext(Dispatchers.Main) {
                        signIn(
                            app.documents.core.network.login.models.request.RequestSignIn(
                                userName = account.name ?: "",
                                accessToken = accessToken,
                                provider = ApiContract.Social.GOOGLE
                            )
                        )
                    }
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
    protected open fun onGooglePermission(intent: Intent?) {}

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