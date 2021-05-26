package app.editors.manager.mvp.presenters.login

import app.documents.core.account.CloudAccount
import app.documents.core.network.models.login.Token
import app.documents.core.network.models.login.request.RequestSignIn
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
        val requestSignIn = RequestSignIn(accessToken = token)
        getUserInfo(requestSignIn, Token(token = token))
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        super.onAccountCreateSuccess(account)
        viewState.onSuccessLogin()
    }
}