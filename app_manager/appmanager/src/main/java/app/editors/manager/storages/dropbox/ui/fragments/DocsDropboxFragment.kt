package app.editors.manager.storages.dropbox.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.editors.manager.app.App
import app.editors.manager.storages.base.fragment.BaseStorageDocsFragment
import app.editors.manager.storages.dropbox.dropbox.login.DropboxLoginHelper
import app.editors.manager.storages.dropbox.mvp.presenters.DocsDropboxPresenter
import app.editors.manager.ui.activities.main.MainActivity
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter
import javax.inject.Inject

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

    @Inject
    lateinit var dropboxLoginHelper: DropboxLoginHelper

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {

                BaseActivity.REQUEST_ACTIVITY_CAMERA -> {
                    mCameraUri?.let { uri ->
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

    override fun getDocsPresenter() = presenter

    override fun onRefreshToken() {
        dropboxLoginHelper.startSignInActivity(this) {
            MainActivity.show(requireContext())
            requireActivity().finish()
        }
    }

}