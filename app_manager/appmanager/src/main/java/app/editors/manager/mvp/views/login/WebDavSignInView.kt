package app.editors.manager.mvp.views.login

import app.documents.core.model.login.OidcConfiguration
import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface WebDavSignInView : BaseView {
    fun onLogin()
    fun onDialogWaiting()
    fun onDialogClose()
    fun onUrlError()
    fun onNextCloudLogin(url: String)
    fun onOwnCloudLogin(url:String, config: OidcConfiguration?)
}