package app.editors.manager.storages.googledrive.mvp.views

import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.mvp.views.main.DocsBaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsGoogleDriveView: DocsBaseView {
    fun onFileWebView(file: CloudFile)
    fun onChooseDownloadFolder()
}