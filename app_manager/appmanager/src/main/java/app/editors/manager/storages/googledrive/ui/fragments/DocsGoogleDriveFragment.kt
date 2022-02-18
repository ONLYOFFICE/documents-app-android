package app.editors.manager.storages.googledrive.ui.fragments

import android.app.Activity
import android.content.Intent
import app.editors.manager.R
import app.editors.manager.storages.googledrive.mvp.presenters.DocsGoogleDrivePresenter
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.storages.base.fragment.BaseStorageDocsFragment
import app.editors.manager.storages.base.view.BaseStorageDocsView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.dialogs.ContextBottomDialog
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

open class DocsGoogleDriveFragment: BaseStorageDocsFragment() {

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
            ContextBottomDialog.Buttons.RENAME -> if (presenter.itemClicked is CloudFile) {
                showEditDialogRename(
                    getString(R.string.dialogs_edit_rename_title),
                    StringUtils.getNameWithoutExtension(presenter.itemTitle),
                    getString(R.string.dialogs_edit_hint),
                    DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                    getString(R.string.dialogs_edit_accept_rename),
                    getString(R.string.dialogs_common_cancel_button))
            } else {
                showEditDialogRename(
                    getString(R.string.dialogs_edit_rename_title),
                    presenter.itemTitle,
                    getString(R.string.dialogs_edit_hint),
                    DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                    getString(R.string.dialogs_edit_accept_rename),
                    getString(R.string.dialogs_common_cancel_button))
            }
            ContextBottomDialog.Buttons.DELETE -> showQuestionDialog(
                getString(R.string.dialogs_question_delete),
                presenter.itemTitle,
                getString(R.string.dialogs_question_accept_remove),
                getString(R.string.dialogs_common_cancel_button),
                DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT
            )
            ContextBottomDialog.Buttons.EXTERNAL -> {
                presenter.externalLink
            }
            ContextBottomDialog.Buttons.EDIT -> {
                presenter.getFileInfo()
            }
        }
    }

    override fun onStateMenuDefault(sortBy: String, isAsc: Boolean) {
        super.onStateMenuDefault(sortBy, isAsc)
        menu?.findItem(R.id.toolbar_sort_item_size)?.isVisible = false
    }

    override fun onDocsBatchOperation() {
        onDialogClose()
        onSnackBar(getString(R.string.operation_complete_message))
        onRefresh()
    }
}