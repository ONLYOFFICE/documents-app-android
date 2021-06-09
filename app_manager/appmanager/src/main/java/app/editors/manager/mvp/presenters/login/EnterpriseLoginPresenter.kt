package app.editors.manager.mvp.presenters.login

import android.content.Intent
import app.documents.core.account.CloudAccount
import app.documents.core.network.models.login.request.RequestSignIn
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.CommonSignInView
import io.reactivex.disposables.Disposable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.StringUtils.isEmailValid
import moxy.InjectViewState

@InjectViewState
open class EnterpriseLoginPresenter : BaseLoginPresenter<CommonSignInView>() {

    companion object {
        val TAG: String = EnterpriseLoginPresenter::class.java.simpleName
        const val TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING"
        const val TAG_DIALOG_LOGIN_FACEBOOK = "TAG_DIALOG_LOGIN_FACEBOOK"
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onTwoFactorAuth(phoneNoise: String?, request: RequestSignIn) {
        viewState.onTwoFactorAuth(phoneNoise, Json.encodeToString(request))
    }

    override fun onTwoFactorAuthApp(secretKey: String?, request: RequestSignIn) {
        viewState.onTwoFactorAuthTfa(secretKey, Json.encodeToString(request))
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        super.onAccountCreateSuccess(account)
        viewState.onSuccessLogin()
    }

    override fun onGooglePermission(intent: Intent) {
        viewState.onGooglePermission(intent)
    }

    fun signInPortal(login: String, password: String, portal: String) {
        networkSettings.setBaseUrl(portal)
        if (!isEmailValid(login)) {
            viewState.onEmailNameError(context.getString(R.string.errors_email_syntax_error))
            return
        }
        viewState.onWaitingDialog(context.getString(R.string.dialogs_sign_in_portal_header_text), TAG_DIALOG_WAITING)
        signIn(RequestSignIn(userName = login, password = password))
    }

}