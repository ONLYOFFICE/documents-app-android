package app.editors.manager.mvp.presenters.login

import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.login.OnlyOfficeCloudView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.InjectViewState

@InjectViewState
class OnlyOfficeCloudPresenter : BasePresenter<OnlyOfficeCloudView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getAccounts() {
        CoroutineScope(Dispatchers.Default).launch {
            val accounts = cloudDataSource.getAccounts()
            withContext(Dispatchers.Main) {
                viewState.checkAccounts(accounts.isEmpty())
            }
        }
    }

}