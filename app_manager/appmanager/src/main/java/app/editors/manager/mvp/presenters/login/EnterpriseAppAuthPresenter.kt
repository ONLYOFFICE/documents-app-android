package app.editors.manager.mvp.presenters.login

import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.EnterpriseAppView
import moxy.InjectViewState

@InjectViewState
class EnterpriseAppAuthPresenter : BaseLoginPresenter<EnterpriseAppView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onAccountCreateSuccess(account: CloudAccount) {
        super.onAccountCreateSuccess(account)
        viewState.onSuccessLogin()
    }
}