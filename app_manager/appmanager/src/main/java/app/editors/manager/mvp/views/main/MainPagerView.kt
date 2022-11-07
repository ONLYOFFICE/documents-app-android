package app.editors.manager.mvp.views.main

import androidx.annotation.StringRes
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface MainPagerView : BaseView {
    fun onError(@StringRes res: Int)
    fun onRender(stringAccount: String, sections: List<Explorer>?)
    fun onFinishRequest()
    fun setFileData(fileData: String)
    fun onOpenProjectFileError(@StringRes res: Int)
    fun onSwitchAccount(data: OpenDataModel)
}