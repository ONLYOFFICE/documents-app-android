package app.editors.manager.mvp.views.main

import app.editors.manager.mvp.presenters.main.MainPagerState
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface MainPagerView: MvpView {

    fun onRender(state: MainPagerState)
}