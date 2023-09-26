package app.editors.manager.ui.fragments.operations

import app.editors.manager.mvp.presenters.storages.DocsDropboxPresenter
import app.editors.manager.ui.fragments.base.BaseStorageOperationsFragment
import app.editors.manager.ui.fragments.storages.DropboxSignInFragment
import moxy.presenter.InjectPresenter

class DocsDropboxOperationFragment : BaseStorageOperationsFragment() {

    companion object {

        val TAG: String = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsDropboxOperationFragment = DocsDropboxOperationFragment()
    }

    @InjectPresenter
    override lateinit var presenter: DocsDropboxPresenter

    override fun getOperationsPresenter() = presenter

    override fun onRefreshToken() {
        showFragment(DropboxSignInFragment.newInstance(), DropboxSignInFragment.TAG, false)
    }

}