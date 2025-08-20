package app.editors.manager.mvp.presenters.login

import app.documents.core.model.cloud.CloudPortal
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.PersonalRegisterView
import kotlinx.coroutines.launch
import lib.toolkit.base.BuildConfig
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.presenterScope
import java.util.Locale

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

    fun checkMail(email: String) {
        var isInfo = false
        var mail = email

        if (email.contains(TAG_INFO)) {
            isInfo = true
            mail = email.replace(TAG_INFO, "")
        }
        if (StringUtils.isEmailValid(mail.trim())) {
            registerPortal(mail, isInfo = isInfo)
        } else {
            viewState.onMessage(R.string.errors_email_syntax_error)
        }
    }

    private fun registerPortal(email: String?, isInfo: Boolean = false) {
        if (email == null || email.isEmpty() && !StringUtils.isEmailValid(email)) {
            viewState.onError(context.getString(R.string.errors_email_syntax_error))
            return
        }

        val url = if (isInfo) "${BuildConfig.SUBDOMAIN}.${BuildConfig.DEFAULT_INFO_HOST}" else ApiContract.PERSONAL_HOST
        App.getApp().refreshLoginComponent(CloudPortal(url = url))
        signInJob = presenterScope.launch {
            loginRepository.registerPersonal(email, Locale.getDefault().language)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Success -> viewState.onRegisterPortal()
                        is NetworkResult.Error -> fetchError(result.exception)
                        is NetworkResult.Loading -> Unit
                    }
                }
        }
    }

}