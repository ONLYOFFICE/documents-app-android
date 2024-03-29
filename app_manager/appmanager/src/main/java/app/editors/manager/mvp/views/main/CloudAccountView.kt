package app.editors.manager.mvp.views.main

import app.documents.core.network.webdav.WebDavService
import app.editors.manager.mvp.presenters.main.CloudAccountState
import app.editors.manager.mvp.views.base.BaseViewExt
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface CloudAccountView : BaseViewExt {
    fun onRender(state: CloudAccountState)
    fun onWebDavLogin(account: String, provider: WebDavService.Providers)
    fun onAccountLogin(portal: String, login: String)
    fun onOneDriveLogin()
    fun onDropboxLogin()
    fun onGoogleDriveLogin()
    fun onSuccessLogin()
    fun onWaiting()
}