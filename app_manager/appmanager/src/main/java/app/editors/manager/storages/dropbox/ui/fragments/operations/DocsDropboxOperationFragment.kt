package app.editors.manager.storages.dropbox.ui.fragments.operations

import app.documents.core.network.ApiContract
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.storages.base.fragment.BaseStorageOperationsFragment
import app.editors.manager.storages.dropbox.mvp.presenters.DocsDropboxPresenter
import app.editors.manager.storages.dropbox.ui.fragments.DropboxSignInFragment
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
import moxy.presenter.InjectPresenter

class DocsDropboxOperationFragment : BaseStorageOperationsFragment() {

    companion object {

        val TAG = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsDropboxOperationFragment = DocsDropboxOperationFragment()
    }


    @InjectPresenter
    override lateinit var presenter: DocsDropboxPresenter

    override fun getOperationsPresenter() = presenter
    override fun onRefreshToken() {
        val storage = Storage(
            ApiContract.Storage.DROPBOX,
            BuildConfig.DROP_BOX_COM_CLIENT_ID,
            BuildConfig.DROP_BOX_COM_REDIRECT_URL
        )
        showFragment(DropboxSignInFragment.newInstance(storage), DropboxSignInFragment.TAG, false)
    }

    init {
        App.getApp().appComponent.inject(this)
    }


}