package app.editors.manager.mvp.presenters.login

import app.documents.core.network.login.LoginResponse
import app.documents.core.network.login.models.request.RequestSignIn
import app.documents.core.network.login.models.response.ResponseAllSettings
import app.documents.core.network.login.models.response.ResponseCapabilities
import app.documents.core.network.login.models.response.ResponseSettings
import app.documents.core.network.login.models.response.ResponseSignIn
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.app.loginService
import app.editors.manager.mvp.views.login.EnterpriseAppView
import io.reactivex.disposables.Disposable
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
        disposable = context.loginService.signIn(requestSignIn, smsCode)
            .doOnSuccess { getCapabilities() }
            .subscribe({ response ->
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

    private fun getCapabilities() {
        disposable = context.loginService.capabilities().subscribe({ response ->
            when (response) {
                is LoginResponse.Success -> {
                    when (val success = response.response) {
                        is ResponseCapabilities -> {
                            networkSettings.ldap = success.response.ldapEnabled
                            networkSettings.ssoUrl = success.response.ssoUrl
                            networkSettings.ssoLabel = success.response.ssoLabel
                        }

                        is ResponseSettings -> {
                            networkSettings.documentServerVersion = success.response.documentServer.orEmpty()
                            networkSettings.serverVersion = success.response.communityServer.orEmpty()
                        }

                        is ResponseAllSettings -> networkSettings.isDocSpace = success.response.docSpace
                    }
                }
                is LoginResponse.Error -> throw response.error
            }
        }, ::fetchError)
    }
}