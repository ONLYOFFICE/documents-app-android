package app.editors.manager.storages.googledrive.ui.fragments.operations

import android.net.Uri
import app.documents.core.network.ApiContract
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.storages.base.fragment.BaseStorageOperationsFragment
import app.editors.manager.storages.base.view.DocsGoogleDriveView
import app.editors.manager.storages.googledrive.mvp.presenters.DocsGoogleDrivePresenter
import app.editors.manager.storages.googledrive.ui.fragments.GoogleDriveSignInFragment
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
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

    override fun onRefreshToken() {
        val storage = Storage(
            ApiContract.Storage.GOOGLEDRIVE,
            BuildConfig.GOOGLE_COM_CLIENT_ID,
            BuildConfig.GOOGLE_COM_REDIRECT_URL
        )
        showFragment(GoogleDriveSignInFragment.newInstance(storage), GoogleDriveSignInFragment.TAG, false)
    }

    init {
        App.getApp().appComponent.inject(this)
    }


}