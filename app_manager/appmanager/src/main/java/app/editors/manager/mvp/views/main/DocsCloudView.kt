package app.editors.manager.mvp.views.main

import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

interface DocsCloudView : DocsBaseView {
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFileWebView(file: CloudFile, isEditMode: Boolean = false)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showMoveCopyDialog(names: ArrayList<String>, action: String, title: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateUpdateFilterMenu()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onArchiveRoom(isArchived: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onArchiveSelectedRooms(rooms: List<Entity>)
}