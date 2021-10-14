package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.Intent
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.main.DocsCloudPresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.mvp.views.main.DocsCloudView
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog.Companion.newInstance
import lib.toolkit.base.managers.utils.TimeUtils.fileTimeStamp
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

abstract class DocsCloudFragment : DocsBaseFragment(), DocsCloudView {

    @InjectPresenter
    lateinit var cloudPresenter: DocsCloudPresenter

    @ProvidePresenter
    fun providePresenter(): DocsCloudPresenter {
        val account = getApp().appComponent.accountOnline
        return account?.let { DocsCloudPresenter(it) }
            ?: throw RuntimeException("Cloud account can't be null")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BaseActivity.REQUEST_ACTIVITY_WEB_VIEWER -> {
                    showViewerActivity(data!!.getSerializableExtra("TAG_FILE") as CloudFile?)
                }
                BaseActivity.REQUEST_ACTIVITY_OPERATION -> {
                    showSnackBar(R.string.operation_complete_message)
                    onRefresh()
                }
                BaseActivity.REQUEST_ACTIVITY_STORAGE -> {
                    val folder =
                        data?.getSerializableExtra(StorageActivity.TAG_RESULT) as CloudFolder?
                    cloudPresenter.addFolderAndOpen(
                        folder,
                        linearLayoutManager!!.findFirstVisibleItemPosition()
                    )
                }
                BaseActivity.REQUEST_ACTIVITY_SHARE -> {
                    if (data?.hasExtra(ShareActivity.TAG_RESULT) == true) {
                        cloudPresenter.setItemsShared(data
                            .getBooleanExtra(ShareActivity.TAG_RESULT, false))
                    }
                }
                BaseActivity.REQUEST_ACTIVITY_CAMERA -> {
                    mCameraUri?.let { uri ->
                        cloudPresenter.upload(uri, null)
                    }
                }
                BaseActivity.REQUEST_ACTIVITY_FILE_PICKER -> {
                    data?.clipData?.let {
                        cloudPresenter.upload(null, it)
                    }
                    data?.data?.let {
                        cloudPresenter.upload(it, null)
                    }
                }
            }
        }
    }

    override fun onActionBarTitle(title: String) {
        if (isActivePage) {
            setActionBarTitle(title)
            if (title == "0") {
                disableMenu()
            }
        }
    }

    override fun onStateMenuSelection() {
        menu?.let { menu ->
            menuInflater?.let { menuInflater ->
                menuInflater.inflate(R.menu.docs_select, menu)
                deleteItem = menu.findItem(R.id.toolbar_selection_delete)
                    .setVisible(cloudPresenter.isContextItemEditable).also {
                        setMenuItemTint(requireContext(), it, lib.toolkit.base.R.color.colorPrimary)
                    }
                moveItem = menu.findItem(R.id.toolbar_selection_move)
                    .setVisible(cloudPresenter.isContextItemEditable)
                restoreItem = menu.findItem(R.id.toolbar_selection_restore)
                copyItem = menu.findItem(R.id.toolbar_selection_copy)
                downloadItem = menu.findItem(R.id.toolbar_selection_download)
                    .setVisible(!cloudPresenter.isTrashMode)
                setAccountEnable(false)
            }
        }
    }

    override fun onUploadFileProgress(progress: Int, id: String) {
        explorerAdapter?.getUploadFileById(id).let { uploadFile ->
            uploadFile?.progress = progress
            explorerAdapter?.updateItem(uploadFile)
        }
    }

    override fun onDeleteUploadFile(id: String) {
        explorerAdapter?.removeUploadItemById(id)
    }

    override fun onRemoveUploadHead() {
        explorerAdapter?.removeHeader(getApp().getString(R.string.upload_manager_progress_title))
    }

    override fun onAddUploadsFile(uploadFiles: List<Entity>) {
        onRemoveUploadHead()
        explorerAdapter?.addItemsAtTop(uploadFiles)
        explorerAdapter?.addItemAtTop(Header(getString(R.string.upload_manager_progress_title)))
        recyclerView?.scrollToPosition(0)
    }

    override fun showMoveCopyDialog(names: ArrayList<String>, action: String, titleFolder: String) {
        moveCopyDialog = newInstance(names, action, titleFolder)
        moveCopyDialog?.dialogButtonOnClick = this
        moveCopyDialog?.show(requireFragmentManager(), MoveCopyDialog.TAG)
    }

    override fun onActionButtonClick(buttons: ActionBottomDialog.Buttons?) {
        super.onActionButtonClick(buttons)
        when (buttons) {
            ActionBottomDialog.Buttons.PHOTO -> if (checkCameraPermission()) {
                showCameraActivity(fileTimeStamp)
            }
            ActionBottomDialog.Buttons.STORAGE -> {
                showStorageActivity(cloudPresenter.isUserSection)
            }
            else -> { }
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        tag?.let {
            when (tag) {
                DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY -> cloudPresenter.emptyTrash()
                DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE -> cloudPresenter.removeShareContext()
            }
        }
    }

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        super.onContextButtonClick(buttons)
        when (buttons) {
            ContextBottomDialog.Buttons.RESTORE -> presenter.moveContext()
            ContextBottomDialog.Buttons.EDIT -> cloudPresenter.onEditContextClick()
            ContextBottomDialog.Buttons.SHARE -> showShareActivity(
                cloudPresenter.itemClicked
            )
            ContextBottomDialog.Buttons.EXTERNAL -> {
                setContextDialogExternalLinkEnable(false)
                cloudPresenter.externalLink
            }
            ContextBottomDialog.Buttons.SHARE_DELETE -> showQuestionDialog(
                getString(R.string.dialogs_question_share_remove),
                cloudPresenter.itemTitle,
                getString(R.string.dialogs_question_share_remove),
                getString(R.string.dialogs_common_cancel_button),
                DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE
            )
            ContextBottomDialog.Buttons.FAVORITE_ADD -> cloudPresenter.addToFavorite()
            ContextBottomDialog.Buttons.FAVORITE_DELETE -> cloudPresenter.deleteFromFavorite()
            else -> { }
        }
    }

    override fun continueClick(tag: String?, action: String?) {
        var operationType = ApiContract.Operation.OVERWRITE
        when (tag) {
            MoveCopyDialog.TAG_DUPLICATE -> operationType = ApiContract.Operation.DUPLICATE
            MoveCopyDialog.TAG_OVERWRITE -> operationType = ApiContract.Operation.OVERWRITE
            MoveCopyDialog.TAG_SKIP -> operationType = ApiContract.Operation.SKIP
        }
        cloudPresenter.transfer(operationType, action != MoveCopyDialog.ACTION_COPY)
    }

    override fun onFileWebView(file: CloudFile) {
        showViewerActivity(file)
    }

    /*
     * On pager scroll callback
     * */
    override fun onScrollPage() {
        cloudPresenter.initViews()
    }

    override fun onResume() {
        super.onResume()
        cloudPresenter.setSectionType(section)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cloudPresenter.setSectionType(section)
    }

    override val isActivePage: Boolean
        get() = isResumed

    override val presenter: DocsBasePresenter<out DocsBaseView?>
        get() = cloudPresenter

    override val isWebDav: Boolean
        get() = false

    protected abstract val section: Int

    private fun disableMenu() {
        menu?.let {
            deleteItem?.isEnabled = false
        }
    }

    val isRoot: Boolean
        get() = presenter.isRoot

    companion object {
        var KEY_ACCOUNT = "key_account"
    }
}