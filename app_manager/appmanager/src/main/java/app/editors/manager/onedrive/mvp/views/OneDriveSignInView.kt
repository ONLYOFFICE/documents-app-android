package app.editors.manager.onedrive.mvp.views

import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface OneDriveSignInView : BaseView {
    fun onLogin()
}