package app.editors.manager.mvp.views.login

import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface EnterpriseCreateSignInView : BaseView {
    fun onSuccessLogin()
    fun onTwoFactorAuth(phoneNoise: String?, request: String?)
    fun onShowProgress()
}