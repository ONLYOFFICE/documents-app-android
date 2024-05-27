package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.View
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.editors.manager.R
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.popup.MainPopupItem
import app.editors.manager.ui.popup.SelectPopupItem
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog

class DocsTrashFragment : DocsCloudFragment() {

    private val isArchive: Boolean get() = section == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
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
        deleteItem?.isVisible = !isArchive
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
                    showRestoreDialog()
                } else {
                    cloudPresenter.moveCopyOperation(OperationsState.OperationType.RESTORE)
                }
            }

            else -> super.onContextButtonClick(contextItem)
        }
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
        UiUtils.showQuestionDialog(
            context = requireContext(),
            title = if (count > 0) {
                resources.getQuantityString(R.plurals.dialogs_question_delete_title, count, count)
            } else {
                getString(R.string.dialogs_question_delete_all_title)
            },
            description = resources.getQuantityString(R.plurals.dialogs_question_message_delete, count),
            acceptTitle = getString(R.string.dialogs_question_accept_delete),
            cancelTitle = getString(R.string.dialogs_common_cancel_button),
            acceptErrorTint = true,
            acceptListener =  {
                if (isResumed && isArchive) {
                    cloudPresenter.deleteRoom()
                } else if (isResumed && section == ApiContract.SectionType.CLOUD_TRASH) {
                    super.onAcceptClick(CommonDialog.Dialogs.QUESTION, null, tag)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        cloudPresenter.isTrashMode = true
    }

    override fun onPause() {
        super.onPause()
        cloudPresenter.isTrashMode = false
    }

    override fun getFilters(): Boolean {
        return if (isArchive) {
            val filter = presenter.preferenceTool.filter
            filter.roomType != RoomFilterType.None || filter.author.id.isNotEmpty()
        } else super.getFilters()
    }

    override val mainActionBarClickListener: (MainPopupItem) -> Unit = { item ->
        if (item == MainPopupItem.EmptyTrash) {
            showDeleteDialog(
                count = -1,
                tag = DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY
            )
        } else super.mainActionBarClickListener(item)
    }

    override val selectActionBarClickListener: (SelectPopupItem) -> Unit = { item ->
        when (item) {
            SelectPopupItem.Operation.Restore -> showRestoreDialog()
            SelectPopupItem.Operation.Delete -> presenter.delete()
            else -> super.selectActionBarClickListener(item)
        }
    }

    override fun showSelectActionPopup(vararg excluded: SelectPopupItem) {
        super.showSelectActionPopup(
            SelectPopupItem.Operation.Move,
            SelectPopupItem.Operation.Copy,
            SelectPopupItem.Download
        )
    }

    private fun showRestoreDialog() {
        val count = cloudPresenter.getSelectedItemsCount() or 1
        UiUtils.showQuestionDialog(
            context = requireContext(),
            title = resources.getQuantityString(R.plurals.rooms_restore_title, count),
            description = resources.getQuantityString(R.plurals.rooms_restore_desc, count),
            acceptTitle = getString(R.string.trash_snackbar_move_button),
            acceptListener = { cloudPresenter.archiveRoom(false) }
        )
    }

    companion object {

        fun newInstance(section: Int, rootPath: String): DocsCloudFragment {
            return DocsTrashFragment().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_PATH, rootPath)
                    putInt(KEY_SECTION, section)
                }
            }
        }
    }
}