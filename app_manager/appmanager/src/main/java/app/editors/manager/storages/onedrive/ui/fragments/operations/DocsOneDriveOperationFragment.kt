package app.editors.manager.storages.onedrive.ui.fragments.operations

import app.editors.manager.app.App
import app.editors.manager.storages.base.fragment.BaseStorageOperationsFragment
import app.editors.manager.storages.onedrive.mvp.presenters.DocsOneDrivePresenter
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
import moxy.presenter.InjectPresenter

class DocsOneDriveOperationFragment: BaseStorageOperationsFragment() {

    companion object {

        val TAG = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsOneDriveOperationFragment = DocsOneDriveOperationFragment()
    }


    @InjectPresenter
    override lateinit var presenter: DocsOneDrivePresenter

    override fun getOperationsPresenter() = presenter

    override fun onRefreshToken() {
        //stub
    }

    init {
        App.getApp().appComponent.inject(this)
    }
}