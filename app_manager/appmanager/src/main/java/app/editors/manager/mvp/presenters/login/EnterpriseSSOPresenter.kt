package app.editors.manager.mvp.presenters.login

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.common.NetworkResult
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
            try {
                loginRepository.signInWithSSO(token)
                    .collect { result ->
                        when (result) {
                            is NetworkResult.Success -> onAccountCreateSuccess(result.data)
                            is NetworkResult.Error -> fetchError(result.exception)
                            is NetworkResult.Loading -> Unit
                        }
                    }
            } catch (e: Exception) {
                fetchError(e)
            }
        }
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        super.onAccountCreateSuccess(account)
        viewState.onSuccessLogin()
    }
}