package app.editors.manager.mvp.presenters.login

import app.documents.core.network.login.LoginResponse
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.app.App
import app.editors.manager.app.loginService
import app.editors.manager.mvp.views.login.PasswordRecoveryView
import io.reactivex.disposables.Disposable
import lib.toolkit.base.managers.utils.StringUtils.isEmailValid
import moxy.InjectViewState

@InjectViewState
class PasswordRecoveryPresenter : BaseLoginPresenter<PasswordRecoveryView>() {

    companion object {
        val TAG: String = PasswordRecoveryPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var mDisposable: Disposable? = null

    fun recoverPassword(email: String, isPersonal: Boolean) {
        if (!isEmailValid(email)) {
            viewState.onEmailError()
        } else {
            sendEmailNotification(email, isPersonal)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable?.dispose()
    }

    private fun sendEmailNotification(email: String, isPersonal: Boolean) {
        try {
            val requestPassword =
                app.documents.core.network.login.models.request.RequestPassword(
                    if (isPersonal) ApiContract.PERSONAL_HOST else networkSettings.getPortal(),
                    email
                )
            mDisposable = context.loginService.passwordRecovery(requestPassword)
                .map {
                    when (it) {
                        is LoginResponse.Success -> viewState.onPasswordRecoverySuccess(email)
                        is LoginResponse.Error -> viewState.onError(it.error.message)
                    }
                }
                .subscribe()
        } catch (error: IllegalArgumentException) {
            viewState.onEmailError()
        }
    }


}