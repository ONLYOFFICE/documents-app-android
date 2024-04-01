package app.editors.manager.mvp.presenters.login

import app.documents.core.network.common.Result
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.mvp.views.login.PasswordRecoveryView
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.StringUtils.isEmailValid
import moxy.InjectViewState
import moxy.presenterScope

@InjectViewState
class PasswordRecoveryPresenter : BaseLoginPresenter<PasswordRecoveryView>() {

    companion object {
        val TAG: String = PasswordRecoveryPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    fun recoverPassword(email: String, isPersonal: Boolean) {
        if (!isEmailValid(email)) {
            viewState.onEmailError()
        } else {
            sendEmailNotification(email, isPersonal)
        }
    }

    private fun sendEmailNotification(email: String, isPersonal: Boolean) {
        signInJob = presenterScope.launch {
            loginRepository.passwordRecovery(
                portal = if (isPersonal) ApiContract.PERSONAL_HOST else context.accountOnline?.portalUrl.orEmpty(),
                email = email
            ).collect { result ->
                when (result) {
                    is Result.Success -> viewState.onPasswordRecoverySuccess(email)
                    is Result.Error -> fetchError(result.exception)
                }
            }
        }
    }
}