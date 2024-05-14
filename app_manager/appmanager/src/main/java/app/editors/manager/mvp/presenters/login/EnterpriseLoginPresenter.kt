package app.editors.manager.mvp.presenters.login

import android.content.Intent
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.login.request.RequestSignIn
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.CommonSignInView
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState

@InjectViewState
open class EnterpriseLoginPresenter : BaseLoginPresenter<CommonSignInView>() {

    companion object {
        val TAG: String = EnterpriseLoginPresenter::class.java.simpleName
        const val TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING"
        const val TAG_DIALOG_LOGIN_FACEBOOK = "TAG_DIALOG_LOGIN_FACEBOOK"
    }

    val currentPortal: CloudPortal?
        get() = App.getApp().loginComponent.currentPortal

    var useLdap: Boolean = false

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

        viewState.onWaitingDialog(context.getString(R.string.dialogs_sign_in_portal_header_text), TAG_DIALOG_WAITING)
        if (App.getApp().loginComponent.currentPortal == null) {
            App.getApp().refreshLoginComponent(portal)
        }

        signInWithEmail(login, password)
    }
}