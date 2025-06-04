package app.editors.manager.mvp.presenters.login

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.login.User
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.common.asResult
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.mvp.views.login.EnterpriseCreateSignInView
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moxy.InjectViewState
import moxy.presenterScope

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
    private val cloudPortal by lazy { App.getApp().loginComponent.currentPortal }

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

    fun createPortal(
        password: String,
        email: String,
        first: String,
        last: String,
        recaptcha: String
    ) {

        // Check user input portal
        val partsPortal = cloudPortal?.url?.split(".").orEmpty()
        if (partsPortal.size != PORTAL_PARTS || partsPortal[PORTAL_PART_NAME].length < PORTAL_LENGTH) {
            viewState.onError(context.getString(R.string.login_api_portal_name))
            return
        }

        // Create api
        viewState.onShowProgress()
        val domain = partsPortal[PORTAL_PART_HOST] + "." + partsPortal[PORTAL_PART_DOMAIN]
        App.getApp().refreshLoginComponent(CloudPortal(url = ApiContract.API_SUBDOMAIN + "." + domain))

        // Validate portal
        signInJob = presenterScope.launch {
            loginRepository.registerPortal(
                portalName = partsPortal[PORTAL_PART_NAME],
                email = email,
                firstName = first,
                lastName = last,
                password = password,
                recaptchaResponse = recaptcha
            ).asResult()
                .collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            FirebaseUtils.addAnalyticsCreatePortal(cloudPortal?.url.orEmpty(), email)
                            App.getApp().refreshLoginComponent(cloudPortal)
                            signInWithEmail(email, password)
                        }
                        is NetworkResult.Error -> fetchError(result.exception)
                        is NetworkResult.Loading -> Unit
                    }
                }
        }
    }
}