package app.editors.manager.storages.googledrive.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.network.ApiContract
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.storages.base.fragment.BaseStorageDocsFragment
import app.editors.manager.storages.base.view.DocsGoogleDriveView
import app.editors.manager.storages.base.work.BaseStorageUploadWork
import app.editors.manager.storages.googledrive.managers.works.UploadWork
import app.editors.manager.storages.googledrive.mvp.presenters.DocsGoogleDrivePresenter
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.popup.MainActionBarPopup
import app.editors.manager.ui.popup.SelectActionBarPopup
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.popup.ActionBarPopupItem
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

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        when (buttons) {
            ContextBottomDialog.Buttons.DOWNLOAD -> onFileDownloadPermission()
            ContextBottomDialog.Buttons.RENAME -> showEditDialogRename(
                getString(R.string.dialogs_edit_rename_title),
                presenter.itemTitle,
                getString(R.string.dialogs_edit_hint),
                DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                getString(R.string.dialogs_edit_accept_rename),
                getString(R.string.dialogs_common_cancel_button),
                presenter.itemExtension
            )

            ContextBottomDialog.Buttons.EXTERNAL -> {
                presenter.externalLink
            }
            ContextBottomDialog.Buttons.EDIT -> {
                presenter.getFileInfo()
            }
            else -> {
                super.onContextButtonClick(buttons)
            }
        }
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
        val storage = Storage(
            ApiContract.Storage.GOOGLEDRIVE,
            BuildConfig.GOOGLE_COM_CLIENT_ID,
            BuildConfig.GOOGLE_COM_REDIRECT_URL
        )
        showFragment(GoogleDriveSignInFragment.newInstance(storage), GoogleDriveSignInFragment.TAG, false)
    }

    override fun showMainActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showMainActionBarMenu(listOf(MainActionBarPopup.Size))
    }

    override fun showSelectedActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showSelectedActionBarMenu(
            listOf<ActionBarPopupItem>(SelectActionBarPopup.Download)
                .takeIf { presenter.isFolderSelected } ?: excluded
        )
    }
}