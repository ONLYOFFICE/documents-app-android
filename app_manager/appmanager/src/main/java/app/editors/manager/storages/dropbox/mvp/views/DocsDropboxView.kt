package app.editors.manager.storages.dropbox.mvp.views

import app.editors.manager.mvp.views.main.DocsBaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsDropboxView : DocsBaseView{
    fun onChooseDownloadFolder()
}