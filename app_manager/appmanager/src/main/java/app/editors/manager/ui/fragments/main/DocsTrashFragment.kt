package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.popup.MainActionBarPopup
import app.editors.manager.ui.popup.SelectActionBarPopup
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.popup.ActionBarPopupItem

class DocsTrashFragment: DocsCloudFragment() {


    private var emptyTrashItem: MenuItem? = null
    private var isEmptyTrashVisible = false
    
    private val isArchive: Boolean get() = section == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM

    override fun onPrepareOptionsMenu(menu: Menu) {
        showMenu()
    }

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
            onItemContextClick(view, position)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (isResumed) {
            emptyTrashItem = menu.findItem(R.id.toolbar_item_empty_trash)
            emptyTrashItem?.isVisible = isEmptyTrashVisible && section != ApiContract.SectionType.CLOUD_ARCHIVE_ROOM
            showMenu()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_item_empty_trash -> {
                showQuestionDialog(
                    getString(R.string.trash_dialog_empty_title),
                    null,
                    getString(R.string.dialogs_question_accept_yes),
                    getString(R.string.dialogs_question_accept_no),
                    DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemContextClick(view: View, position: Int) {
        val item = explorerAdapter?.getItem(position) as Item
        cloudPresenter.onContextClick(item, position, true)
    }

    override fun setMenuMainEnabled(isEnabled: Boolean) {
        super.setMenuMainEnabled(isEnabled)
        isEmptyTrashVisible = isEnabled
        emptyTrashItem?.isVisible = isEmptyTrashVisible && section != ApiContract.SectionType.CLOUD_ARCHIVE_ROOM
        searchItem?.isVisible = isEnabled
    }

    private fun showMenu() {
        if (cloudPresenter.isSelectionMode) {
            deleteItem?.isVisible = true
        } else {
            setActionBarTitle("")
            emptyTrashItem?.let { item ->
                UiUtils.setMenuItemTint(requireContext(), item, lib.toolkit.base.R.color.colorPrimary)
            }
        }
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

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        when (buttons) {
            ContextBottomDialog.Buttons.DELETE -> showQuestionDialog(
                getString(R.string.trash_popup_delete),
                cloudPresenter.itemTitle,
                getString(R.string.dialogs_question_accept_delete),
                getString(R.string.dialogs_common_cancel_button),
                DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT
            )
            ContextBottomDialog.Buttons.RESTORE -> {
                if (isArchive) {
                    cloudPresenter.archiveRoom(false)
                } else {
                    cloudPresenter.moveCopyOperation(OperationsState.OperationType.RESTORE)
                }
            }
            else -> {
            }
        }
        contextBottomDialog?.dismiss()
    }

    override fun onDeleteBatch(list: List<Entity>) {
        if (list.isEmpty()) {
            setMenuMainEnabled(false)
        }
        super.onDeleteBatch(list)
    }

    override fun onResume() {
        super.onResume()
        cloudPresenter.isTrashMode = true
    }

    override fun onPause() {
        super.onPause()
        cloudPresenter.isTrashMode = false
    }

    override fun showSelectedActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showSelectedActionBarMenu(excluded = mutableListOf(
            SelectActionBarPopup.Move,
            SelectActionBarPopup.Copy,
            SelectActionBarPopup.Download
        ).apply {
            if (isArchive) add(SelectActionBarPopup.Restore)
        })
    }

    override fun showMainActionBarMenu(excluded: List<ActionBarPopupItem>) {
        val sortItems = MainActionBarPopup.sortPopupItems.toMutableList().apply {
            add(MainActionBarPopup.RoomType)
            add(MainActionBarPopup.RoomTags)
        }
        super.showMainActionBarMenu(if (isArchive) sortItems else excluded)
    }

    override fun setMenuFilterEnabled(isEnabled: Boolean) {
        filterItem?.isVisible = !isArchive
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