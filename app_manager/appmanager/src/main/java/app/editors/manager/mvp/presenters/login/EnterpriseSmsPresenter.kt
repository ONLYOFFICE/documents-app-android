package app.editors.manager.mvp.presenters.login

import app.documents.core.storage.account.CloudAccount
import app.documents.core.network.login.LoginResponse
import app.documents.core.network.login.models.request.RequestSignIn
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.loginService
import app.editors.manager.mvp.views.login.EnterpriseSmsView
import io.reactivex.disposables.Disposable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moxy.InjectViewState

@InjectViewState
class EnterpriseSmsPresenter : BaseLoginPresenter<EnterpriseSmsView>() {

    companion object {
        val TAG: String = EnterpriseSmsPresenter::class.java.simpleName
    }

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

    fun signInPortal(smsCode: String?, request: String) {
        val requestSignIn = Json.decodeFromString<RequestSignIn>(request)
        disposable = context.loginService.signIn(requestSignIn, smsCode)
            .subscribe({ response ->
                when (response) {
                    is LoginResponse.Success -> {
                        signInSuccess(requestSignIn, (response.response as app.documents.core.network.login.models.response.ResponseSignIn).response)
                    }
                    is LoginResponse.Error -> {
                        fetchError(response.error)
                    }
                }
            }, {
                fetchError(it)
            })
    }

    fun resendSms(request: String) {
        val requestNumber = Json.decodeFromString<RequestSignIn>(request)
        disposable = context.loginService.sendSms(
            RequestSignIn(
                userName = requestNumber.userName,
                password = requestNumber.password,
                accessToken = requestNumber.accessToken,
                provider = requestNumber.provider
            )
        )
            .subscribe({ response ->
                if (response is LoginResponse.Success) {
                    viewState.onResendSms()
                } else {
                    viewState.onError(context.getString(R.string.errors_client_portal_sms))
                }
            }) { throwable -> fetchError(throwable) }
    }
}