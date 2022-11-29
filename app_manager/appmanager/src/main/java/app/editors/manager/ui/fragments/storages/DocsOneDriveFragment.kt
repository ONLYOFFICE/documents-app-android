package app.editors.manager.ui.fragments.storages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.editors.manager.app.App
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import app.editors.manager.mvp.presenters.storages.DocsOneDrivePresenter
import app.editors.manager.ui.popup.SelectActionBarPopup
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.popup.ActionBarPopupItem
import moxy.presenter.InjectPresenter

class DocsOneDriveFragment : BaseStorageDocsFragment() {

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


    override fun showSelectedActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showSelectedActionBarMenu(mutableListOf<ActionBarPopupItem>().apply {
            if (!presenter.isFoldersInSelection()) add(SelectActionBarPopup.Download)
        })
    }

    override fun onRefreshToken() {
        //stub
    }

}