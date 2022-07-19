package app.editors.manager.storages.googledrive.ui.fragments

import android.app.Activity
import android.content.Intent
import app.documents.core.network.ApiContract
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.storages.base.fragment.BaseStorageDocsFragment
import app.editors.manager.storages.googledrive.mvp.presenters.DocsGoogleDrivePresenter
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.popup.MainActionBarPopup
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.popup.ActionBarPopupItem
import moxy.presenter.InjectPresenter

class DocsGoogleDriveFragment: BaseStorageDocsFragment() {

    companion object {
        val TAG = DocsGoogleDriveFragment::class.java.simpleName

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
                BaseActivity.REQUEST_ACTIVITY_OPERATION -> {
                    onRefresh()
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
            }
        }
    }

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        when (buttons) {
            ContextBottomDialog.Buttons.MOVE -> presenter.moveContext()
            ContextBottomDialog.Buttons.COPY -> presenter.copy()
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
        }
    }

    override fun onDocsBatchOperation() {
        onDialogClose()
        onSnackBar(getString(R.string.operation_complete_message))
        onRefresh()
    }

    override fun onRefreshToken() {
        val storage = Storage(
            ApiContract.Storage.GOOGLEDRIVE,
            BuildConfig.GOOGLE_COM_CLIENT_ID,
            BuildConfig.GOOGLE_COM_REDIRECT_URL
        )
        showFragment(GoogleDriveSignInFragment.newInstance(storage), GoogleDriveSignInFragment.TAG, false)
    }

    override fun showMainActionBarMenu(itemId: Int, excluded: List<ActionBarPopupItem>) {
        super.showMainActionBarMenu(
            itemId = itemId,
            excluded = listOf(MainActionBarPopup.Size)
        )
    }
}