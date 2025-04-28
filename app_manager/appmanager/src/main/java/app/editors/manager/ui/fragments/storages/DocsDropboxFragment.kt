package app.editors.manager.ui.fragments.storages

import android.app.Activity
import android.content.Intent
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.storages.DocsDropboxPresenter
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import app.editors.manager.ui.fragments.base.StorageLoginFragment
import moxy.presenter.InjectPresenter

class DocsDropboxFragment: BaseStorageDocsFragment() {

    companion object {
        val TAG: String = DocsDropboxFragment::class.java.simpleName


        fun newInstance() = DocsDropboxFragment()
    }

    @InjectPresenter
    override lateinit var presenter: DocsDropboxPresenter

    override fun getSection(): ApiContract.Section = ApiContract.Section.Storage.Dropbox

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onEditorActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onEditorActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_DOCS, REQUEST_SHEETS, REQUEST_PRESENTATION -> data?.data?.let { uri ->
                    if(data.getBooleanExtra(KEY_MODIFIED, false)) {
                        presenter.upload(
                            uri,
                            null,
                            KEY_UPDATE
                        )
                    }
                }
            }
        }
    }

    override fun getDocsPresenter() = presenter

    override fun onAuthorization() {
        showFragment(
            StorageLoginFragment.newInstance(DropboxUtils.storage),
            StorageLoginFragment.TAG,
            false
        )
    }

}