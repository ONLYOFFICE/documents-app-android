package app.editors.manager.mvp.presenters.login

import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.EnterpriseAppView
import moxy.InjectViewState

@InjectViewState
class EnterpriseAppAuthPresenter : BaseLoginPresenter<EnterpriseAppView>() {

    init {
        App.getApp().appComponent.inject(this)
    }
}