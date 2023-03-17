package app.editors.manager.mvp.views.storages

import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface GoogleDriveSignInView: BaseView {
    fun onLogin()
}