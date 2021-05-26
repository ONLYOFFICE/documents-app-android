package app.editors.manager.mvp.presenters.login

import app.documents.core.login.LoginResponse
import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.request.RequestPassword
import app.editors.manager.app.App
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.mvp.views.login.PasswordRecoveryView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import lib.toolkit.base.managers.utils.StringUtils.isEmailValid
import moxy.InjectViewState

@InjectViewState
class PasswordRecoveryPresenter : BaseLoginPresenter<PasswordRecoveryView>() {


    companion object {
        val TAG = PasswordRecoveryPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var mDisposable: Disposable? = null

    fun recoverPassword(email: String, isPersonal: Boolean) {
        if (!isEmailValid(email)) {
            viewState!!.onEmailError()
        } else {
            sendEmailNotification(email, isPersonal)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDisposable != null) {
            mDisposable!!.dispose()
        }
    }

    private fun sendEmailNotification(email: String, isPersonal: Boolean) {
        val requestPassword = RequestPassword( if(isPersonal) ApiContract.PERSONAL_HOST else networkSettings.getPortal(), email)
        mDisposable = App.getApp().loginComponent.loginService.passwordRecovery(requestPassword)
            .map {
                when(it){
                    is LoginResponse.Success -> viewState.onPasswordRecoverySuccess(email)
                    is LoginResponse.Error -> viewState.onError(it.error.message)
                }
            }
            .subscribe()
    }


}