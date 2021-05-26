package app.editors.manager.mvp.views.login

import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface EnterprisePhoneView : BaseView {
    fun onSuccessChange(request: String)
}