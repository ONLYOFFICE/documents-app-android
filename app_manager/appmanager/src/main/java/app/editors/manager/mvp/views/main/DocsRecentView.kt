package app.editors.manager.mvp.views.main

import app.documents.core.account.Recent
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.presenters.main.OpenState
import app.editors.manager.mvp.presenters.main.RecentState
import app.editors.manager.ui.dialogs.ContextBottomDialog
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsRecentView : DocsBaseView {
    fun updateFiles(files: List<Recent>)
    fun openFile(response: CloudFile)
    fun onMoveElement(recent: Recent, position: Int)
    fun onContextShow(state: ContextBottomDialog.State)
    fun onDeleteItem(position: Int)
    fun onReverseSortOrder(itemList: List<Recent>)
    fun onRecentGet(list: List<Recent>)

    fun onRender(state: RecentState)
    fun onOpenFile(state: OpenState)
}