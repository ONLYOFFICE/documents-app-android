package app.editors.manager.mvp.presenters.login

import app.documents.core.storage.account.CloudAccount
import app.documents.core.network.login.LoginResponse
import app.documents.core.network.login.models.request.RequestSignIn
import app.documents.core.network.login.models.response.ResponseSignIn
import app.editors.manager.app.App
import app.editors.manager.app.loginService
import app.editors.manager.mvp.views.login.EnterpriseAppView
import io.reactivex.disposables.Disposable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moxy.InjectViewState

@InjectViewState
class EnterpriseAppAuthPresenter : BaseLoginPresenter<EnterpriseAppView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        viewState.onSuccessLogin()
    }

    fun signInPortal(smsCode: String, request: String?) {
        val requestSignIn = Json.decodeFromString<RequestSignIn>(request ?: "")
        disposable = context.loginService.signIn(requestSignIn, smsCode).subscribe({ response ->
            when (response) {
                is LoginResponse.Success -> {
                    getUserInfo(requestSignIn, (response.response as ResponseSignIn).response)
                }
                is LoginResponse.Error -> {
                    fetchError(response.error)
                }
            }
        }, { fetchError(it) })
    }
}