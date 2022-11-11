package app.editors.manager.mvp.views.main

import android.net.Uri
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.presenters.main.OpenState
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsOnDeviceView : DocsBaseView {
    fun onActionDialog()
    fun onRemoveItem(item: Item)
    fun onRemoveItems(items: List<Item>)
    fun onShowCamera(photoUri: Uri)
    fun onShowFolderChooser()
    fun onShowPortals()
    fun onOpenMedia(state: OpenState.Media)

    //Open file
    fun onShowDocs(uri: Uri, isNew: Boolean = false)
    fun onShowCells(uri: Uri)
    fun onShowSlides(uri: Uri)
    fun onShowPdf(uri: Uri)
}