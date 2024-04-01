package app.editors.manager.ui.fragments.storages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.storages.DocsDropboxPresenter
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import app.editors.manager.ui.fragments.base.StorageLoginFragment
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

class DocsDropboxFragment: BaseStorageDocsFragment() {

    companion object {
        val TAG: String = DocsDropboxFragment::class.java.simpleName


        fun newInstance(account: String) = DocsDropboxFragment().apply {
            arguments = Bundle(1).apply {
                putString(KEY_ACCOUNT, account)
            }
        }
    }

    @InjectPresenter
    override lateinit var presenter: DocsDropboxPresenter

    override fun getSection(): ApiContract.Section = ApiContract.Section.Storage.Dropbox

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {

                BaseActivity.REQUEST_ACTIVITY_CAMERA -> {
                    cameraUri?.let { uri ->
                        presenter.upload(uri, null, KEY_UPLOAD)
                    }
                }
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