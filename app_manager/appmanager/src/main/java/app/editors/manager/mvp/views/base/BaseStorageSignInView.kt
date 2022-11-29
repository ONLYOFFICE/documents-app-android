package app.editors.manager.mvp.views.base

import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface BaseStorageSignInView: BaseView {
    fun onLogin()
    fun onStartLogin()
}