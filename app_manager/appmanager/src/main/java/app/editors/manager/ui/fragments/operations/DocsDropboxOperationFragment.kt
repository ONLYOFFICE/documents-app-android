package app.editors.manager.ui.fragments.operations

import app.editors.manager.app.App
import app.editors.manager.ui.fragments.base.BaseStorageOperationsFragment
import app.documents.core.network.storages.dropbox.login.DropboxLoginHelper
import app.editors.manager.mvp.presenters.storages.DocsDropboxPresenter
import app.editors.manager.ui.activities.main.MainActivity
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class DocsDropboxOperationFragment : BaseStorageOperationsFragment() {

    companion object {

        val TAG: String = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsDropboxOperationFragment = DocsDropboxOperationFragment()
    }


    @InjectPresenter
    override lateinit var presenter: DocsDropboxPresenter

    @Inject
    lateinit var dropboxLoginHelper: DropboxLoginHelper

    override fun getOperationsPresenter() = presenter

    override fun onRefreshToken() {
        dropboxLoginHelper.startSignInActivity(this) {
            App.getApp().refreshDropboxInstance()
            MainActivity.show(requireContext())
            requireActivity().finish()
        }
    }

    init {
        App.getApp().appComponent.inject(this)
    }


}