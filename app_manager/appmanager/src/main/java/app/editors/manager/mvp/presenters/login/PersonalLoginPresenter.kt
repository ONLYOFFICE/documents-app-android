package app.editors.manager.mvp.presenters.login

import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.app.App
import moxy.InjectViewState

@InjectViewState
class PersonalLoginPresenter : EnterpriseLoginPresenter() {

    companion object {
        val TAG: String = PersonalLoginPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    fun signInPersonal(login: String, password: String) {
        signInPortal(login.trim { it <= ' ' }, password, ApiContract.PERSONAL_HOST)
    }

}