package app.editors.manager.mvp.views.login

import androidx.annotation.StringRes
import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface PersonalRegisterView : BaseView {
    fun onRegisterPortal()
    fun onWaitingDialog()
    fun onMessage(@StringRes message: Int)
}