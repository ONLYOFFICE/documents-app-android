package app.editors.manager.mvp.views.main

import androidx.annotation.StringRes
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
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
    fun onCreateRoom(type: Int = 2, item: Item, isCopy: Boolean = false)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onLeaveRoomDialog(@StringRes title: Int, @StringRes question: Int, isOwner: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showSetOwnerFragment(cloudFolder: CloudFolder)

}