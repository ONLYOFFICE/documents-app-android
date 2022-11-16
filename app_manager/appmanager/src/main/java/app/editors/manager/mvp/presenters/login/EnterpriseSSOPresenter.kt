package app.editors.manager.mvp.presenters.login

import app.documents.core.storage.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.EnterpriseSSOView

class EnterpriseSSOPresenter : BaseLoginPresenter<EnterpriseSSOView>() {

    companion object {
        val TAG: String = EnterpriseSSOPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    fun signInWithSSO(token: String) {
        val requestSignIn = app.documents.core.network.login.models.request.RequestSignIn(accessToken = token)
        getUserInfo(requestSignIn, app.documents.core.network.login.models.Token(token = token))
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        super.onAccountCreateSuccess(account)
        viewState.onSuccessLogin()
    }
}