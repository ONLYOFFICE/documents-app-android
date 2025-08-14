package app.editors.manager.mvp.presenters.login

import android.content.Intent
import android.util.Log
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.common.utils.SocialSignIn
import app.documents.core.network.common.utils.SocialUtils
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.CommonSignInView
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.presenterScope

@InjectViewState
open class EnterpriseLoginPresenter : BaseLoginPresenter<CommonSignInView>() {

    companion object {
        val TAG: String = EnterpriseLoginPresenter::class.java.simpleName
        const val TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING"
        const val TAG_DIALOG_LOGIN_FACEBOOK = "TAG_DIALOG_LOGIN_FACEBOOK"
    }

    private val twitterRepository
        get() = App.getApp().loginComponent.twitterLoginRepository

    val currentPortal: CloudPortal?
        get() = App.getApp().loginComponent.currentPortal

    var useLdap: Boolean = false

    var socialSignIn: SocialSignIn? = null
        private set

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onTwoFactorAuth(phoneNoise: String?, request: RequestSignIn) {
        super.onTwoFactorAuth(phoneNoise, request)
        viewState.onTwoFactorAuth(phoneNoise, Json.encodeToString(request))
    }

    override fun onTwoFactorAuthApp(secretKey: String?, request: RequestSignIn) {
        viewState.onTwoFactorAuthTfa(secretKey, Json.encodeToString(request))
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        super.onAccountCreateSuccess(account)
        viewState.onSuccessLogin()
    }

    override fun onGooglePermission(intent: Intent?) {
        intent?.let { viewState.onGooglePermission(it) }
    }

    fun signInPortal(login: String, password: String, portal: CloudPortal) {
        if (!useLdap && !StringUtils.isEmailValid(login)) {
            viewState.onEmailNameError(context.getString(R.string.errors_email_syntax_error))
            return
        }

        viewState.onWaitingDialog()
        if (App.getApp().loginComponent.currentPortal == null) {
            App.getApp().refreshLoginComponent(portal)
        }

        signInWithEmail(login, password)
    }

    fun signInPortalSocial(social: String) {
        socialSignIn = SocialUtils.getSocialSignIn(social)
        socialSignIn?.let { social ->
            if (social is SocialSignIn.Twitter) {
                viewState.onWaitingDialog()
                signInJob = presenterScope.launch {
                    twitterRepository.getRequestToken(
                        consumerKey = social.clientId,
                        consumerSecret = social.SECRET_KEY,
                        callbackUrl = social.callbackUrl
                    ).collect { result ->
                        if (result is NetworkResult.Success) {
                            viewState.onSocialAuth(social.authUrl, result.data)
                        }
                        if (result is NetworkResult.Error) {
                            fetchError(result.exception)
                        }
                    }
                }
            } else {
                viewState.onSocialAuth(socialSignIn)
            }
        }
    }

    fun requestAccessToken(oauthToken: String, oauthVerifier: String){
        presenterScope.launch {
            twitterRepository.getAccessToken(oauthToken, oauthVerifier).collect { result ->
                if (result is NetworkResult.Success) {
                    //TODO
                    Log.d("TAG", "oauthToken = ${result.data.oauthToken}")
                    Log.d("TAG", "oauthTokenSecret = ${result.data.oauthTokenSecret}")
                }
            }
        }
    }
}