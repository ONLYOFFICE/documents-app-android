package app.editors.manager.mvp.views.login

import androidx.annotation.StringRes
import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface EnterpriseCreateValidateView : BaseView {
    fun onValidatePortalSuccess(email: String?, first: String?, last: String?)
    fun onPortalNameError(message: String)
    fun onEmailNameError(message: String)
    fun onFirstNameError(message: String)
    fun onLastNameError(message: String)
    fun onRegionDomain(domain: String)
    fun onShowWaitingDialog(@StringRes title: Int)
}