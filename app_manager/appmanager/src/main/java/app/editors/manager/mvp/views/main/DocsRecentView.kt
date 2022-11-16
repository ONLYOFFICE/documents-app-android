package app.editors.manager.mvp.views.main

import app.documents.core.storage.recent.Recent
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.presenters.main.OpenState
import app.editors.manager.mvp.presenters.main.RecentState
import app.editors.manager.ui.dialogs.ContextBottomDialog
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsRecentView : DocsBaseView {
    fun updateFiles(files: List<Recent>, sortBy: String, sortOrder: String)
    fun openFile(response: CloudFile)
    fun onContextShow(state: ContextBottomDialog.State)
    fun onDeleteItem(position: Int)
    fun onRender(state: RecentState)
    fun onOpenFile(state: OpenState)
}