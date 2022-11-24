package app.editors.manager.mvp.presenters.login

import app.documents.core.network.login.LoginResponse
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.loginService
import app.editors.manager.mvp.views.login.PersonalRegisterView
import io.reactivex.disposables.Disposable
import lib.toolkit.base.BuildConfig
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import java.util.*

@InjectViewState
class PersonalSignUpPresenter : BaseLoginPresenter<PersonalRegisterView>() {

    companion object {
        val TAG: String = PersonalSignUpPresenter::class.java.simpleName
        private const val EMAIL_CODE = "201"

        const val TAG_INFO = "/#info"
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    override fun cancelRequest() {
        disposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun registerPortal(email: String?, isInfo: Boolean = false) {
        if (isInfo) {
            networkSettings.setBaseUrl("${BuildConfig.SUBDOMAIN}.${BuildConfig.DEFAULT_INFO_HOST}")
        } else {
            networkSettings.setBaseUrl(ApiContract.PERSONAL_HOST)
        }


        email?.let {
            disposable = context.loginService
                .registerPersonal(
                    app.documents.core.network.login.models.request.RequestRegister(
                        email = email,
                        language = Locale.getDefault().language
                    )
                )
                .subscribe({ loginResponse ->
                    when (loginResponse) {
                        is LoginResponse.Success -> {
                            checkResponse(loginResponse.response as app.documents.core.network.login.models.response.ResponseRegisterPersonalPortal)
                        }
                        is LoginResponse.Error -> {
                            fetchError(loginResponse.error)
                        }
                    }
                }, { error ->
                    fetchError(error)
                })
        }
    }

    private fun checkResponse(response: app.documents.core.network.login.models.response.ResponseRegisterPersonalPortal) {
        if (!response.response.isNullOrEmpty() && response.status != EMAIL_CODE) {
            viewState.onError(context.getString(R.string.errors_email_already_registered))
        } else if (!response.response.isNullOrEmpty()) {
            viewState.onError(response.response)
        } else {
            viewState.onRegisterPortal()
        }
    }

    fun checkMail(email: String) {
        var isInfo = false
        var mail = email

        if (email.contains(TAG_INFO)) {
            isInfo = true
            mail = email.replace(TAG_INFO, "")
        }
        if (StringUtils.isEmailValid(mail.trim())) {
            viewState.onWaitingDialog()
            registerPortal(mail, isInfo = isInfo)
        } else {
            viewState.onMessage(R.string.errors_email_syntax_error)
        }
    }

}