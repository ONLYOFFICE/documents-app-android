package app.editors.manager.ui.fragments.storages

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.providers.BaseFileProvider
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.storages.DocsDropboxPresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import app.editors.manager.ui.fragments.base.StorageLoginFragment
import moxy.presenter.InjectPresenter

class DocsDropboxFragment: BaseStorageDocsFragment() {

    companion object {
        val TAG: String = DocsDropboxFragment::class.java.simpleName


        fun newInstance() = DocsDropboxFragment()
    }

    @InjectPresenter
    override lateinit var storagePresenter: DocsDropboxPresenter

    override val presenter: DocsBasePresenter<out DocsBaseView, out BaseFileProvider>
        get() = storagePresenter

    override fun getSection(): ApiContract.Section = ApiContract.Section.Storage.Dropbox

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onAuthorization() {
        showFragment(
            StorageLoginFragment.newInstance(DropboxUtils.storage),
            StorageLoginFragment.TAG,
            false
        )
    }

}