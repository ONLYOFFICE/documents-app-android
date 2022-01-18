package app.editors.manager.mvp.views.main

import app.documents.core.webdav.WebDavApi
import app.editors.manager.mvp.presenters.main.CloudAccountState
import app.editors.manager.mvp.views.base.BaseView
import app.editors.manager.mvp.views.base.BaseViewExt
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface CloudAccountView : BaseViewExt {
    fun onRender(state: CloudAccountState)
    fun onWebDavLogin(account: String, provider: WebDavApi.Providers)
    fun onAccountLogin(portal: String, login: String)
    fun onOneDriveLogin()
    fun onDropboxLogin()
    fun onSuccessLogin()
}