package app.editors.manager.mvp.presenters.login

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.network.common.Result
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.EnterpriseSmsView
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import moxy.InjectViewState
import moxy.presenterScope

@InjectViewState
class EnterpriseSmsPresenter : BaseLoginPresenter<EnterpriseSmsView>() {

    companion object {
        val TAG: String = EnterpriseSmsPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        viewState.onSuccessLogin()
    }

    fun signInPortal(smsCode: String?, request: String) {
        val requestSignIn = Json.decodeFromString<RequestSignIn>(request)
        signInWithEmail(requestSignIn.userName, requestSignIn.password, smsCode)
    }

    fun resendSms(request: String) {
        val requestNumber = Json.decodeFromString<RequestSignIn>(request)
        signInJob = presenterScope.launch {
            loginRepository.sendSms(
                userName = requestNumber.userName,
                password = requestNumber.password,
                accessToken = requestNumber.accessToken,
                provider = requestNumber.provider
            ).collect { result ->
                when (result) {
                    is Result.Success -> viewState.onResendSms()
                    is Result.Error -> viewState.onError(context.getString(R.string.errors_client_portal_sms))
                }
            }
        }
    }
}