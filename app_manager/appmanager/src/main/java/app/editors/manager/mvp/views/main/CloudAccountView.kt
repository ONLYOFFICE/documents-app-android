package app.editors.manager.mvp.views.main

import app.documents.core.model.cloud.WebdavProvider
import app.editors.manager.mvp.presenters.main.CloudAccountState
import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface CloudAccountView : BaseView {
    fun onRender(state: CloudAccountState)
    fun onWebDavLogin(account: String, provider: WebdavProvider)
    fun onAccountLogin(portal: String?, login: String?)
    fun onOneDriveLogin()
    fun onDropboxLogin()
    fun onGoogleDriveLogin()
    fun onSuccessLogin()
    fun onWaiting()
    fun onHideDialog()
}