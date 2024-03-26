package app.editors.manager.mvp.views.main

import app.documents.core.model.cloud.Recent
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.mvp.presenters.main.OpenState
import app.editors.manager.mvp.presenters.main.RecentState
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsRecentView : DocsBaseView {
    fun updateFiles(files: List<Recent>, sortByUpdated: Boolean)
    fun openFile(response: CloudFile)
    fun onDeleteItem(position: Int)
    fun onRender(state: RecentState)
    fun onOpenFile(state: OpenState)
}