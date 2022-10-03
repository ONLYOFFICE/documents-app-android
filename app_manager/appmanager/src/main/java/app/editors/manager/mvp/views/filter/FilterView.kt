package app.editors.manager.mvp.views.filter

import app.editors.manager.mvp.views.base.BaseView
import app.editors.manager.mvp.views.main.DocsBaseView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

interface FilterView : BaseView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun updateViewState(isChanged: Boolean = true)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFilterReset()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFilterProgress()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFilterResult(count: Int)
}