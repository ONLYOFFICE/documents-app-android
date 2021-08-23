package app.editors.manager.mvp.views.main

import androidx.annotation.StringRes
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.presenters.main.MainPagerState
import app.editors.manager.mvp.views.base.BaseView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface MainPagerView : BaseView {
    fun onError(@StringRes res: Int)
    fun onRender(state: MainPagerState)
    fun onRender(stringAccount: String, sections: List<Explorer>?)
    fun onFinishRequest()
    fun setFileData(fileData: String)
}