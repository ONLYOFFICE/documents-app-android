package app.editors.manager.storages.onedrive.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.storages.base.fragment.BaseStorageDocsFragment
import app.editors.manager.storages.base.view.BaseStorageDocsView
import app.editors.manager.storages.onedrive.mvp.presenters.DocsOneDrivePresenter
import app.editors.manager.ui.activities.main.ActionButtonFragment
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

open class DocsOneDriveFragment : BaseStorageDocsFragment() {

    companion object {
        val TAG = DocsOneDriveFragment::class.java.simpleName

        fun newInstance(account: String) = DocsOneDriveFragment().apply {
            arguments = Bundle(1).apply {
                putString(KEY_ACCOUNT, account)
            }
        }
    }

    @InjectPresenter
    override lateinit var presenter: DocsOneDrivePresenter

    override fun getDocsPresenter() = presenter

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isActivePage && resultCode == Activity.RESULT_CANCELED) {
            onRefresh()
        } else if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_DOCS, REQUEST_SHEETS, REQUEST_PRESENTATION -> data?.data?.let { uri ->
                    if(data.getBooleanExtra(KEY_MODIFIED, false)) {
                        presenter.upload(
                            uri,
                            null,
                            KEY_UPDATE
                        )
                    }
                }
                BaseActivity.REQUEST_ACTIVITY_FILE_PICKER -> data?.clipData?.let { clipData ->
                    presenter.upload(
                        null,
                        clipData,
                        KEY_UPLOAD)
                }.run {
                    presenter.upload(data?.data, null, KEY_UPLOAD)
                }
                BaseActivity.REQUEST_ACTIVITY_OPERATION -> {
                    onRefresh()
                }
            }
        }
    }

    override fun onError(message: String?) {
        when(message) {
            context?.getString(R.string.errors_client_unauthorized) -> {
                presenter.refreshToken()
            }
            else -> {
                message?.let { showSnackBar(it) }
            }
        }

    }

    override fun onStateUpdateSelection(isSelection: Boolean) {
        super.onStateUpdateSelection(isSelection)
        if(presenter.isFoldersInSelection()) {
            downloadItem = menu?.findItem(R.id.toolbar_selection_download)?.setVisible(true)
        } else {
            downloadItem = menu?.findItem(R.id.toolbar_selection_download)?.setVisible(false)
        }
    }

}