package app.editors.manager.mvp.views.login

import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface EnterprisePortalView : BaseView {
    fun onSuccessPortal(portal: String, providers: Array<String>)
    fun onHttpPortal(portal: String, providers: Array<String>)
    fun onPortalSyntax(message: String)
    fun onLoginPortal(portal: String)
    fun onShowDialog()
}