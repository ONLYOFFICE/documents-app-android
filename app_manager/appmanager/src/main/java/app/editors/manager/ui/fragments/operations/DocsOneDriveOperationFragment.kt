package app.editors.manager.ui.fragments.operations

import app.editors.manager.app.App
import app.editors.manager.ui.fragments.base.BaseStorageOperationsFragment
import app.editors.manager.mvp.presenters.storages.DocsOneDrivePresenter
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