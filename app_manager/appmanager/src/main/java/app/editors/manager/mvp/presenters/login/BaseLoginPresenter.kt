package app.editors.manager.mvp.presenters.login

import android.content.Intent
import app.documents.core.account.AccountPreferences
import app.documents.core.login.LoginRepository
import app.documents.core.login.LoginResult
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.login.User
import app.documents.core.model.login.request.RequestSignIn
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.base.BaseView
import com.google.android.gms.auth.UserRecoverableAuthException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moxy.presenterScope
import javax.inject.Inject

abstract class BaseLoginPresenter<View : BaseView> : BasePresenter<View>() {

    @Inject
    lateinit var accountPreferences: AccountPreferences

    protected val loginRepository: LoginRepository
        get() = App.getApp().loginComponent.loginRepository

    protected var signInJob: Job? = null

    override fun cancelRequest() {
        signInJob?.cancel()
    }

    fun checkSocialProvider(url: String, onProviders: (List<String>) -> Unit) {
        presenterScope.launch {
            val cloudPortal = App.getApp().loginComponent.currentPortal ?: cloudDataSource.getPortal(url)
            onProviders(cloudPortal?.socialProviders.orEmpty())
        }
    }

    fun signInWithEmail(email: String, password: String, smsCode: String? = null) {
        signInJob = presenterScope.launch {
            loginRepository.signInByEmail(email, password, smsCode)
                .collect { result ->
                    when (result) {
                        is LoginResult.Success -> onAccountCreateSuccess(result.cloudAccount)
                        is LoginResult.Error -> fetchError(result.exception)
                        is LoginResult.Sms -> onTwoFactorAuth(result.phoneNoise, RequestSignIn(email, password))
                        is LoginResult.Tfa -> onTwoFactorAuthApp(result.key, RequestSignIn(email, password))
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
                        is LoginResult.Error -> when (val exception = result.exception) {
                            is UserRecoverableAuthException -> onGooglePermission(exception.intent)
                            else -> fetchError(exception)
                        }
                        is LoginResult.Sms -> onTwoFactorAuth(
                            result.phoneNoise,
                            RequestSignIn(accessToken = accessToken.orEmpty(), provider = provider)
                        )
                        is LoginResult.Tfa -> onTwoFactorAuthApp(
                            result.key,
                            RequestSignIn(accessToken = accessToken.orEmpty(), provider = provider)
                        )
                    }
                }
        }
    }

    protected open fun onTwoFactorAuth(phoneNoise: String?, request: RequestSignIn) {
        preferenceTool.phoneNoise = phoneNoise
    }

    protected open fun onAccountCreateSuccess(account: CloudAccount) {
        App.getApp().refreshCoreComponent()
        //        FirebaseUtils.addAnalyticsLogin(account.portal.portal, account.portal.provider.toString())
    }

    protected open fun onTwoFactorAuthApp(secretKey: String?, request: RequestSignIn) {}

    protected open fun onGetUser(user: User) {}

    protected open fun onGooglePermission(intent: Intent?) {}

}