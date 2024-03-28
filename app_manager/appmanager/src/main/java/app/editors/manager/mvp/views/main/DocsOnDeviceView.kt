package app.editors.manager.mvp.views.main

import android.net.Uri
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.mvp.presenters.main.OpenState
import lib.toolkit.base.OpenMode
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsOnDeviceView : DocsBaseView {
    fun onActionDialog()
    fun onRemoveItems(vararg items: Item)
    fun onShowPortals()
    fun onOpenMedia(state: OpenState.Media)

    //Open file
    fun onShowDocs(uri: Uri, openMode: OpenMode)
    fun onShowCells(uri: Uri)
    fun onShowSlides(uri: Uri)
    fun onShowPdf(uri: Uri)
}