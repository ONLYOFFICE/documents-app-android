package app.editors.manager.mvp.views.main

import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.storage.recent.Recent
import app.editors.manager.mvp.presenters.main.OpenState
import app.editors.manager.mvp.presenters.main.RecentState
import lib.toolkit.base.OpenMode
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsRecentView : DocsBaseView {
    fun updateFiles(files: List<Recent>, sortByUpdated: Boolean)
    fun openFile(response: CloudFile, openMode: OpenMode)
    fun onDeleteItem(position: Int)
    fun onRender(state: RecentState)
    fun onOpenFile(state: OpenState)
}