package app.editors.manager.mvp.views.main

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

interface DocsFilterView : MvpView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun updateViewState()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFilterReset()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFilterProgress()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFilterResult(count: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun handleError(message: String?)
}