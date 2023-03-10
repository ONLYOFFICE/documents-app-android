package app.editors.manager.storages.onedrive.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import app.editors.manager.app.App
import app.editors.manager.storages.base.fragment.BaseStorageDocsFragment
import app.editors.manager.storages.onedrive.mvp.presenters.DocsOneDrivePresenter
import app.editors.manager.ui.fragments.main.DocsOnDeviceFragment
import app.editors.manager.ui.popup.SelectActionBarPopup
import lib.toolkit.base.managers.utils.CameraPicker
import lib.toolkit.base.managers.utils.RequestPermissions
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.popup.ActionBarPopupItem
import moxy.presenter.InjectPresenter

class DocsOneDriveFragment : BaseStorageDocsFragment() {

    companion object {
        val TAG: String = DocsOneDriveFragment::class.java.simpleName

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

    override fun onShowCamera(photoUri: Uri) {
        RequestPermissions(requireActivity().activityResultRegistry, { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                CameraPicker(requireActivity().activityResultRegistry, { isCreate ->
                    if (isCreate) {
                        presenter.upload(photoUri, null, KEY_UPLOAD)
                    } else {
                        presenter.deletePhoto()
                    }
                }, photoUri).show()
            } else {
                presenter.deletePhoto()
            }
        }, arrayOf(Manifest.permission.CAMERA)).request()
    }

}