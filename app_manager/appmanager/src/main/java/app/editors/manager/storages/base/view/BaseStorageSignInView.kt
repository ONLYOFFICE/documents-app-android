package app.editors.manager.storages.base.view

import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface BaseStorageSignInView: BaseView {
    fun onLogin()
    fun onStartLogin()
}