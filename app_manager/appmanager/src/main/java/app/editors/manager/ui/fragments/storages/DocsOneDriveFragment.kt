package app.editors.manager.ui.fragments.storages

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.providers.BaseFileProvider
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.storages.DocsOneDrivePresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import app.editors.manager.ui.fragments.base.StorageLoginFragment
import lib.toolkit.base.managers.utils.CameraPicker
import lib.toolkit.base.managers.utils.RequestPermissions
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

class DocsOneDriveFragment : BaseStorageDocsFragment() {

    companion object {
        val TAG: String = DocsOneDriveFragment::class.java.simpleName

        fun newInstance() = DocsOneDriveFragment()
    }

    @InjectPresenter
    override lateinit var storagePresenter: DocsOneDrivePresenter

    override val presenter: DocsBasePresenter<out DocsBaseView, out BaseFileProvider>
        get() = storagePresenter

    override fun getSection(): ApiContract.Section = ApiContract.Section.Storage.OneDrive

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onEditorActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onEditorActivityResult(requestCode, resultCode, data)
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

    override fun onAuthorization() {
        showFragment(
            StorageLoginFragment.newInstance(OneDriveUtils.storage),
            StorageLoginFragment.TAG,
            false
        )
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