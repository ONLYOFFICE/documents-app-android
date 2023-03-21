package app.editors.manager.mvp.presenters.login

import app.documents.core.storage.account.CloudAccount
import app.documents.core.network.login.LoginResponse
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.login.models.User
import app.documents.core.network.login.models.request.RequestRegister
import app.documents.core.network.login.models.request.RequestSignIn
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.app.loginService
import app.editors.manager.mvp.views.login.EnterpriseCreateSignInView
import io.reactivex.disposables.Disposable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moxy.InjectViewState

@InjectViewState
class EnterpriseCreateLoginPresenter : BaseLoginPresenter<EnterpriseCreateSignInView>() {

    companion object {
        val TAG: String = EnterpriseCreateLoginPresenter::class.java.simpleName
        private const val PORTAL_PARTS = 3
        private const val PORTAL_PART_NAME = 0
        private const val PORTAL_PART_HOST = 1
        private const val PORTAL_PART_DOMAIN = 2
        private const val PORTAL_LENGTH = 6
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    override fun cancelRequest() {
        super.cancelRequest()
        disposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onTwoFactorAuth(phoneNoise: String?, request: RequestSignIn) {
        super.onTwoFactorAuth(phoneNoise, request)
        viewState.onTwoFactorAuth(phoneNoise, Json.encodeToString(request))
    }

    override fun onGetUser(user: User) {
        super.onGetUser(user)
        viewState.onSuccessLogin()
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        super.onAccountCreateSuccess(account)
        viewState.onSuccessLogin()
    }

    fun createPortal(password: String, email: String, first: String, last: String, recaptcha: String) {

        // Check user input portal
        val portal = networkSettings.getPortal()
        val partsPortal = networkSettings.getPortal().split(".")
        if (partsPortal.size != PORTAL_PARTS || partsPortal[PORTAL_PART_NAME].length < PORTAL_LENGTH) {
            viewState.onError(context.getString(R.string.login_api_portal_name))
            return
        }

        // Create api
        viewState.onShowProgress()
        val domain = partsPortal[PORTAL_PART_HOST] + "." + partsPortal[PORTAL_PART_DOMAIN]
        networkSettings.setBaseUrl(ApiContract.API_SUBDOMAIN + "." + domain)

        // Validate portal
        val requestRegister = RequestRegister(
            portalName = partsPortal[PORTAL_PART_NAME],
            email = email,
            firstName = first,
            lastName = last,
            password = password,
            recaptchaResponse = recaptcha
        )
        disposable = context.loginService.registerPortal(requestRegister)
            .subscribe({ loginResponse ->
                when (loginResponse) {
                    is LoginResponse.Success -> {
                        networkSettings.setBaseUrl(portal)
                        FirebaseUtils.addAnalyticsCreatePortal(networkSettings.getPortal(), email)
                        signIn(
                            RequestSignIn(
                                userName = email,
                                password = password
                            )
                        )
                    }
                    is LoginResponse.Error -> {
                        fetchError(loginResponse.error)
                    }
                }
            }) { throwable: Throwable ->
                fetchError(throwable)
            }
    }

}

