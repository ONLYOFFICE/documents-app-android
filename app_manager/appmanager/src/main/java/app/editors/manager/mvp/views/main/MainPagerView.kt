package app.editors.manager.mvp.views.main

import androidx.annotation.StringRes
import app.editors.manager.mvp.presenters.main.MainPagerState
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface MainPagerView: MvpView {

    fun onError(@StringRes res: Int)
    fun onRender(state: MainPagerState)
    fun setFileData(fileData: String)
}