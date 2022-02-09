package app.editors.manager.storages.dropbox.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.editors.manager.storages.base.fragment.BaseStorageDocsFragment
import app.editors.manager.storages.base.view.BaseStorageDocsView
import app.editors.manager.storages.dropbox.mvp.presenters.DocsDropboxPresenter
import app.editors.manager.storages.dropbox.mvp.views.DocsDropboxView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

class DocsDropboxFragment: BaseStorageDocsFragment(), ActionButtonFragment, BaseStorageDocsView {

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_MULTIPLE_FILES_DOWNLOAD -> {
                    data?.data?.let { presenter.download(it) }
                }
                REQUEST_DOCS, REQUEST_SHEETS, REQUEST_PRESENTATION -> data?.data?.let {
                    if(data.getBooleanExtra(KEY_MODIFIED, false)) {
                        presenter.upload(
                            it,
                            null,
                            KEY_UPDATE
                        )
                    }
                }
                BaseActivity.REQUEST_ACTIVITY_FILE_PICKER -> data?.clipData?.let {
                    presenter.upload(
                        null,
                        it,
                        KEY_UPLOAD
                    )
                }.run {
                    presenter.upload(data?.data, null, KEY_UPLOAD)
                }
                BaseActivity.REQUEST_ACTIVITY_OPERATION -> {
                    onRefresh()
                }
            }
        }
    }

    override fun getOtherPresenter() = presenter

}