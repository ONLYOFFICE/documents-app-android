package app.editors.manager.ui.fragments.operations

import android.net.Uri
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.storages.DocsGoogleDrivePresenter
import app.editors.manager.mvp.views.base.DocsGoogleDriveView
import app.editors.manager.ui.fragments.base.BaseStorageOperationsFragment
import app.editors.manager.ui.fragments.base.StorageLoginFragment
import moxy.presenter.InjectPresenter

class DocsGoogleDriveOperationFragment: BaseStorageOperationsFragment(), DocsGoogleDriveView {

    companion object {

        val TAG: String = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsGoogleDriveOperationFragment = DocsGoogleDriveOperationFragment()
    }

    @InjectPresenter
    override lateinit var presenter: DocsGoogleDrivePresenter

    override fun getOperationsPresenter() = presenter

    override fun onUpload(uploadUris: List<Uri>, folderId: String, fileId: String, tag: String) {
        // Stub
    }

    override fun onSignIn() {
        // Stub
    }

    override fun onAuthorization() {
        showFragment(
            StorageLoginFragment.newInstance(GoogleDriveUtils.storage),
            StorageLoginFragment.TAG,
            false
        )
    }

    init {
        App.getApp().appComponent.inject(this)
    }


}