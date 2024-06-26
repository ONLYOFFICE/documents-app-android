package app.editors.manager.mvp.presenters.login

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.common.Result
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.EnterpriseSSOView
import kotlinx.coroutines.launch
import moxy.presenterScope

class EnterpriseSSOPresenter : BaseLoginPresenter<EnterpriseSSOView>() {

    companion object {
        val TAG: String = EnterpriseSSOPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    fun signInWithSSO(token: String) {
        signInJob = presenterScope.launch {
            loginRepository.signInWithSSO(token)
                .collect { result ->
                    when (result) {
                        is Result.Success -> onAccountCreateSuccess(result.result)
                        is Result.Error -> fetchError(result.exception)
                    }
                }
        }
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        super.onAccountCreateSuccess(account)
        viewState.onSuccessLogin()
    }
}