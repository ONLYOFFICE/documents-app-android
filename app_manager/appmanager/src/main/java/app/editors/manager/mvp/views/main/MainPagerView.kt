package app.editors.manager.mvp.views.main

import app.editors.manager.mvp.presenters.main.MainPagerState
import app.editors.manager.mvp.views.base.BaseView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface MainPagerView: BaseView {

    fun onRender(state: MainPagerState)
}