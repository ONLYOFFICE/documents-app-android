package app.editors.manager.storages.dropbox.ui.fragments.operations

import app.editors.manager.app.App
import app.editors.manager.storages.base.fragment.BaseStorageOperationsFragment
import app.editors.manager.storages.dropbox.dropbox.login.DropboxLoginHelper
import app.editors.manager.storages.dropbox.mvp.presenters.DocsDropboxPresenter
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class DocsDropboxOperationFragment : BaseStorageOperationsFragment() {

    companion object {

        val TAG = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsDropboxOperationFragment = DocsDropboxOperationFragment()
    }


    @InjectPresenter
    override lateinit var presenter: DocsDropboxPresenter

    @Inject
    lateinit var dropboxLoginHelper: DropboxLoginHelper

    override fun getOperationsPresenter() = presenter

    override fun onRefreshToken() {
        dropboxLoginHelper.startSignInActivity(this) {
            MainActivity.show(requireContext())
            requireActivity().finish()
        }
    }

    init {
        App.getApp().appComponent.inject(this)
    }


}