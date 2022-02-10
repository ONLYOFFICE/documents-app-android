package app.editors.manager.storages.googledrive.ui.fragments.operations

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.storages.dropbox.ui.fragments.operations.DocsDropboxOperationFragment
import app.editors.manager.storages.googledrive.mvp.presenters.DocsGoogleDrivePresenter
import app.editors.manager.storages.googledrive.mvp.views.DocsGoogleDriveView
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.storages.base.fragment.BaseStorageOperationsFragment
import app.editors.manager.storages.base.presenter.BaseStorageDocsPresenter
import app.editors.manager.storages.base.view.BaseStorageDocsView
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
import moxy.presenter.InjectPresenter

class DocsGoogleDriveOperationFragment: BaseStorageOperationsFragment(), OperationActivity.OnActionClickListener, BaseStorageDocsView {

    companion object {

        val TAG = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsGoogleDriveOperationFragment = DocsGoogleDriveOperationFragment()
    }

    @InjectPresenter
    override lateinit var presenter: DocsGoogleDrivePresenter

    override fun getOperationsPresenter() = presenter

    init {
        App.getApp().appComponent.inject(this)
    }


}