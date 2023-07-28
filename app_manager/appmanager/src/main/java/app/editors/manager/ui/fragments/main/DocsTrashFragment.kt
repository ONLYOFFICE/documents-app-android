package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.View
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.editors.manager.R
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.popup.MainPopupItem
import app.editors.manager.ui.popup.SelectPopupItem
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog

class DocsTrashFragment: DocsCloudFragment() {

    private val isArchive: Boolean get() = section == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        when (tag) {
            DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_SELECTED -> {
                if (isResumed && isArchive) {
                    cloudPresenter.deleteRoom()
                } else if (isResumed && section == ApiContract.SectionType.CLOUD_TRASH) {
                    super.onAcceptClick(dialogs, value, tag)
                }
            }
            else -> super.onAcceptClick(dialogs, value, tag)
        }
    }

    override fun onItemClick(view: View, position: Int) {
        if (presenter.isSelectionMode) {
            super.onItemClick(view, position)
        } else {
            onItemContextClick(position)
        }
    }

    override fun setMenuMainEnabled(isEnabled: Boolean) {
        super.setMenuMainEnabled(isEnabled)
        searchItem?.isVisible = isEnabled
    }

    override fun onStateMenuSelection() {
        super.onStateMenuSelection()
        deleteItem?.isVisible = cloudPresenter.isSelectionMode
    }

    private fun initViews() {
        recyclerView?.let {
            it.setPadding(
                it.paddingLeft, it.paddingTop,
                it.paddingLeft, 0
            )
            cloudPresenter.checkBackStack()
        }
    }

    override fun onContextButtonClick(contextItem: ExplorerContextItem) {
        when (contextItem) {
            is ExplorerContextItem.Delete -> showDeleteDialog(tag = DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT)
            is ExplorerContextItem.Restore -> {
                if (isArchive) {
                    cloudPresenter.archiveRoom(false)
                } else {
                    cloudPresenter.moveCopyOperation(OperationsState.OperationType.RESTORE)
                }
            }
            else -> super.onContextButtonClick(contextItem)
        }
        contextBottomDialog?.dismiss()
    }

    override fun onDeleteBatch(list: List<Entity>) {
        if (list.isEmpty()) {
            setMenuMainEnabled(false)
        }
        super.onDeleteBatch(list)
    }

    override fun onDeleteMessage(count: Int) {
        onSnackBar(resources.getQuantityString(R.plurals.operation_delete_irretrievably, count))
    }

    override fun showDeleteDialog(count: Int, toTrash: Boolean, tag: String) {
        super.showDeleteDialog(count, false, tag)
    }

    override fun onResume() {
        super.onResume()
        cloudPresenter.isTrashMode = true
    }

    override fun onPause() {
        super.onPause()
        cloudPresenter.isTrashMode = false
    }

    override fun setMenuFilterEnabled(isEnabled: Boolean) {
        filterItem?.isVisible = !isArchive
    }

    override val mainActionBarClickListener: (MainPopupItem) -> Unit = { item ->
        if (item == MainPopupItem.EmptyTrash) {
            showDeleteDialog(
                count = -1,
                tag = DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY
            )
        } else super.mainActionBarClickListener(item)
    }

    override fun showSelectActionPopup(vararg excluded: SelectPopupItem) {
        super.showSelectActionPopup(
            SelectPopupItem.Operation.Move,
            SelectPopupItem.Operation.Copy,
            SelectPopupItem.Download
        )
    }

    companion object {

        fun newInstance(stringAccount: String, section: Int, rootPath: String): DocsCloudFragment {
            return DocsTrashFragment().apply {
                arguments = Bundle(3).apply {
                    putString(KEY_ACCOUNT, stringAccount)
                    putString(KEY_PATH, rootPath)
                    putInt(KEY_SECTION, section)
                }
            }
        }
    }
}