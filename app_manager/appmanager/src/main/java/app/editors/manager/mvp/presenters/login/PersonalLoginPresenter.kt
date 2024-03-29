package app.editors.manager.mvp.presenters.login

import android.accounts.Account
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.app.App
import app.editors.manager.app.loginService
import io.reactivex.disposables.Disposable
import moxy.InjectViewState

@InjectViewState
class PersonalLoginPresenter : EnterpriseLoginPresenter() {

    companion object {
        val TAG: String = PersonalLoginPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun initPersonal(): Boolean {
        networkSettings.setDefault()
        networkSettings.setBaseUrl(ApiContract.PERSONAL_HOST)
        disposable = context.loginService
            .serverVersion()
            .subscribe { loginResponse ->
                    networkSettings.serverVersion = loginResponse.response as String
            }
        return true
    }

    fun signInPersonal(login: String, password: String) {
        if (initPersonal()) {
            signInPortal(login.trim { it <= ' ' }, password, networkSettings.getPortal())
        }
    }

    fun signInPersonalWithTwitter(token: String) {
        if (initPersonal()) {
            signInWithTwitter(token)
        }
    }

    fun signInPersonalWithGoogle(token: Account) {
        if (initPersonal()) {
            signInWithGoogle(token)
        }
    }

    fun signInPersonalWithFacebook(token: String) {
        if (initPersonal()) {
            signInWithFacebook(token)
        }
    }

}