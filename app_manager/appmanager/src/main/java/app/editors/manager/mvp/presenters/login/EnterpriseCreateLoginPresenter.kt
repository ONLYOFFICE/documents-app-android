package app.editors.manager.mvp.presenters.login

import app.documents.core.login.LoginResponse
import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.request.RequestRegister
import app.documents.core.network.models.login.request.RequestSignIn
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.mvp.models.user.User
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
        private const val APP_KEY = "android-39ed-4f49-89a4-01fe9175dc91"
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
        viewState.onTwoFactorAuth(phoneNoise, Json.encodeToString(request))
    }

    override fun onGetUser(user: User) {
        super.onGetUser(user)
        viewState.onSuccessLogin()
    }

    fun createPortal(password: String, email: String, first: String, last: String) {

        // Check user input portal
        val portal = networkSettings.getPortal()
        val partsPortal = networkSettings.getPortal().split(".")
        if (partsPortal.size != PORTAL_PARTS || partsPortal[PORTAL_PART_NAME].length < PORTAL_LENGTH) {
            viewState.onError(context.getString(R.string.login_api_portal_name))
            return
        }

        // Create api
        val domain = partsPortal[PORTAL_PART_HOST] + "." + partsPortal[PORTAL_PART_DOMAIN]
        networkSettings.setBaseUrl(ApiContract.API_SUBDOMAIN + "." + domain)
        val loginService = App.getApp().appComponent.loginService

        // Validate portal
        val requestRegister = RequestRegister(
            portalName = partsPortal[PORTAL_PART_NAME],
            email = email,
            firstName = first,
            lastName = last,
            password = password,
            appKey = APP_KEY
        )
        disposable = loginService.registerPortal(requestRegister)
            .subscribe({ loginResponse ->

                when (loginResponse) {
                    is LoginResponse.Success -> {
                        networkSettings.setBaseUrl(portal)
                        FirebaseUtils.addAnalyticsCreatePortal(networkSettings.getPortal(), email);
                        signIn(RequestSignIn(userName = email, password = password))
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

