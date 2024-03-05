package app.editors.manager.mvp.presenters.login

import android.content.Intent
import app.documents.core.login.LoginRepository
import app.documents.core.login.LoginResult
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.login.models.User
import app.documents.core.network.login.models.request.RequestSignIn
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.base.BaseView
import com.google.android.gms.auth.UserRecoverableAuthException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moxy.presenterScope
import javax.inject.Inject

abstract class BaseLoginPresenter<View : BaseView> : BasePresenter<View>() {

    @Inject
    lateinit var loginRepository: LoginRepository

    protected var signInJob: Job? = null

    override fun cancelRequest() {
        signInJob?.cancel()
    }

    fun signInWithEmail(email: String, password: String, smsCode: String? = null) {
        signInJob = presenterScope.launch {
            loginRepository.signInByEmail(email, password, smsCode)
                .collect { result ->
                    when (result) {
                        is LoginResult.Success -> onAccountCreateSuccess(result.cloudAccount)
                        is LoginResult.Error -> {
                            when (val exception = result.exception) {
                                is UserRecoverableAuthException -> onGooglePermission(exception.intent)
                                else -> fetchError(exception)
                            }
                        }
                        else -> Unit
                        //                        is LoginResult.Sms -> onTwoFactorAuth(result.phoneNoise)
                        //                        is LoginResult.Tfa -> onTwoFactorAuthApp(result.key)
                    }
                }
        }
    }

    fun signInWithProvider(accessToken: String?, provider: String) {
        signInJob = presenterScope.launch {
            loginRepository.signInWithProvider(accessToken, provider)
                .collect { result ->
                    when (result) {
                        is LoginResult.Success -> onAccountCreateSuccess(result.cloudAccount)
                        is LoginResult.Error -> fetchError(result.exception)
                        else -> Unit
                    }
                }
        }
    }

    protected open fun onTwoFactorAuth(phoneNoise: String?, request: RequestSignIn) {
        preferenceTool.phoneNoise = phoneNoise
    }

    protected open fun onAccountCreateSuccess(account: CloudAccount) {
        FirebaseUtils.addAnalyticsLogin(account.portal.portal, account.portal.provider.toString())
    }

    protected open fun onTwoFactorAuthApp(secretKey: String?, request: RequestSignIn) {}

    protected open fun onGetUser(user: User) {}

    protected open fun onGooglePermission(intent: Intent?) {}

    //        protected open fun isConfigConnection(t: Throwable?): Boolean {
    //            if (t is SSLHandshakeException && !networkSettings.getCipher() && networkSettings.getScheme() == ApiContract.SCHEME_HTTPS && Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
    //                networkSettings.setCipher(true)
    //                return true
    //            } else if ((t is ConnectException || t is SocketTimeoutException || t is SSLHandshakeException ||
    //                        t is SSLPeerUnverifiedException) && networkSettings.getScheme() == ApiContract.SCHEME_HTTPS
    //            ) {
    //                networkSettings.setCipher(false)
    //                networkSettings.setScheme(ApiContract.SCHEME_HTTP)
    //                return true
    //            }
    //            return false
    //        }
    //
    //        protected fun getPortal(url: String): String? {
    //            if (isValidUrl(url)) {
    //                networkSettings.setScheme(if (URLUtil.isHttpsUrl(url)) ApiContract.SCHEME_HTTPS else ApiContract.SCHEME_HTTP)
    //                return getUrlWithoutScheme(url)
    //            } else {
    //                val concatUrl = networkSettings.getScheme() + url
    //                if (isValidUrl(concatUrl)) {
    //                    return url
    //                }
    //            }
    //            return null
    //        }
}