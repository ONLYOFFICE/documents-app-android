package app.editors.manager.ui.fragments.operations

import app.documents.core.network.common.utils.DropboxUtils
import app.editors.manager.mvp.presenters.storages.DocsDropboxPresenter
import app.editors.manager.ui.fragments.base.BaseStorageOperationsFragment
import app.editors.manager.ui.fragments.base.StorageLoginFragment
import moxy.presenter.InjectPresenter

class DocsDropboxOperationFragment : BaseStorageOperationsFragment() {

    companion object {

        val TAG: String = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsDropboxOperationFragment = DocsDropboxOperationFragment()
    }

    @InjectPresenter
    override lateinit var presenter: DocsDropboxPresenter

    override fun getOperationsPresenter() = presenter

    override fun onAuthorization() {
        showFragment(
            StorageLoginFragment.newInstance(DropboxUtils.storage),
            StorageLoginFragment.TAG,
            false
        )
    }

}