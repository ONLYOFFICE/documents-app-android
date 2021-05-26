package app.editors.manager.mvp.views.login

import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface PasswordRecoveryView : BaseView {
    fun onPasswordRecoverySuccess(email: String)
    fun onEmailError()
}