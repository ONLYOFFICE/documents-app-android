package app.editors.manager.mvp.views.main

import androidx.annotation.StringRes
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Lifetime
import app.editors.manager.viewModels.main.CopyItems
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
    fun onConversionQuestion()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onConversionProgress(progress: Int, extension: String? = null)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showAddRoomFragment(type: Int = 2, copyItems: CopyItems? = null)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showEditRoomFragment(room: CloudFolder)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onLeaveRoomDialog(@StringRes title: Int, @StringRes question: Int, isOwner: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showSetOwnerFragment(cloudFolder: CloudFolder)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showFillFormChooserFragment()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onRoomLifetime(lifetime: Lifetime?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onRoomFileIndexing(indexing: Boolean)
}