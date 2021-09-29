package app.editors.manager.mvp.presenters.login

import app.documents.core.login.LoginResponse
import app.documents.core.network.models.login.request.RequestNumber
import app.documents.core.network.models.login.request.RequestSignIn
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.login.EnterprisePhoneView
import io.reactivex.disposables.Disposable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moxy.InjectViewState

@InjectViewState
class EnterprisePhonePresenter : BasePresenter<EnterprisePhoneView>() {

    companion object {
        val TAG: String = EnterprisePhonePresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun setPhone(newPhone: String?, request: String) {
        val service = App.getApp().loginComponent.loginService
        val requestSignIn = Json.decodeFromString<RequestSignIn>(request)
        disposable = newPhone?.let {
            RequestNumber(
                mobilePhone = it,
                provider = requestSignIn.provider,
                userName = requestSignIn.userName,
                password = requestSignIn.password,
                accessToken = requestSignIn.accessToken
            )
        }?.let {
            service.changeNumber(it)
                .subscribe({ response ->
                    if (response is LoginResponse.Success) {
                        preferenceTool.phoneNoise = newPhone
                        viewState.onSuccessChange(Json.encodeToString(it))
                    } else {
                        viewState.onError((response as LoginResponse.Error).error.message)
                    }
                }) { throwable -> fetchError(throwable) }
        }
    }
}