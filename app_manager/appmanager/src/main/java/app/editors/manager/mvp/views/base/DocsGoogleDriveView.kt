package app.editors.manager.mvp.views.base

import android.net.Uri
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsGoogleDriveView : BaseStorageDocsView {
    fun onUpload(uploadUris: List<Uri>, folderId: String, fileId: String, tag: String) {}
    fun onSignIn() {}
}