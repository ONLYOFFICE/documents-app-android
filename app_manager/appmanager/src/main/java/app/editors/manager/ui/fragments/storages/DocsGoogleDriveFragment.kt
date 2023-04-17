package app.editors.manager.ui.fragments.storages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.manager.models.base.Entity
import app.editors.manager.R
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.managers.works.googledrive.UploadWork
import app.editors.manager.mvp.presenters.storages.DocsGoogleDrivePresenter
import app.editors.manager.mvp.views.base.DocsGoogleDriveView
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import app.editors.manager.ui.popup.MainPopupItem
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

class DocsGoogleDriveFragment: BaseStorageDocsFragment(), DocsGoogleDriveView {

    companion object {
        val TAG: String = DocsGoogleDriveFragment::class.java.simpleName

        fun newInstance(): DocsGoogleDriveFragment = DocsGoogleDriveFragment()

    }

    @InjectPresenter
    override lateinit var presenter: DocsGoogleDrivePresenter

    override fun getDocsPresenter() = presenter

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
                        presenter.upload(uri, null, KEY_UPDATE)
                    }
                }
            }
        }
    }

    override fun onActionButtonClick(buttons: ActionBottomDialog.Buttons?) {
        if (buttons == ActionBottomDialog.Buttons.UPLOAD) {
            showMultipleFilePickerActivity { uris ->
                if (!uris.isNullOrEmpty()) {
                    presenter.upload(null, uris, KEY_UPLOAD)
                }
            }
        } else {
            super.onActionButtonClick(buttons)
        }
    }

    override fun onDocsNext(list: List<Entity>?) {
        explorerAdapter?.addItems(list)
    }

    override fun onDocsBatchOperation() {
        onDialogClose()
        onSnackBar(getString(R.string.operation_complete_message))
        onRefresh()
    }

    override fun onUpload(uploadUris: List<Uri>, folderId: String, fileId: String, tag: String) {
        val workManager = WorkManager.getInstance(requireContext())

        for (uploadUri in uploadUris) {
            val data = Data.Builder()
                .putString(BaseStorageUploadWork.TAG_FOLDER_ID, folderId)
                .putString(BaseStorageUploadWork.TAG_UPLOAD_FILES, uploadUri.toString())
                .putString(BaseStorageUploadWork.KEY_TAG, tag)
                .putString(UploadWork.KEY_FILE_ID, fileId)
                .build()

            val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
                .setInputData(data)
                .build()

            workManager.enqueue(request)
        }
    }

    override fun onRefreshToken() { }

    override fun onSignIn() {
        showFragment(GoogleDriveSignInFragment.newInstance(GoogleDriveUtils.storage), GoogleDriveSignInFragment.TAG, false)
    }

    override fun showMainActionPopup(vararg excluded: MainPopupItem) {
        super.showMainActionPopup(MainPopupItem.SortBy.Size)
    }

}