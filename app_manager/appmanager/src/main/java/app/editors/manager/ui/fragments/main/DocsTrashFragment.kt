package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.managers.providers.CloudFileProvider
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.ui.dialogs.ContextBottomDialog
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint

class DocsTrashFragment : DocsCloudFragment(), View.OnClickListener {

    private var emptyTrashItem: MenuItem? = null
    private var isEmptyTrashVisible = false

    override fun onPrepareOptionsMenu(menu: Menu) {
        showMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        if (isVisible) {
            emptyTrashItem = menu.findItem(R.id.toolbar_item_empty_trash)
            emptyTrashItem?.isVisible = isEmptyTrashVisible
            showMenu()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toolbar_item_empty_trash) {
            showQuestionDialog(
                getString(R.string.trash_dialog_empty_title),
                null,
                getString(R.string.dialogs_question_accept_yes),
                getString(R.string.dialogs_question_accept_no),
                DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSwipeRefresh(): Boolean {
        if (!super.onSwipeRefresh()) {
            cloudPresenter.getItemsById(ID)
            return true
        }
        return false
    }

    override fun onScrollPage() {
        super.onScrollPage()
        cloudPresenter.getItemsById(ID)
        initViews()
    }

    override fun onClick(v: View) {
        cloudPresenter.moveContext()
    }

    override fun onItemContextClick(view: View, position: Int) {
        val item = explorerAdapter?.getItem(position) as Item
        cloudPresenter.onContextClick(item, position, true)
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        swipeRefreshLayout?.isRefreshing = true
        cloudPresenter.getItemsById(ID)
    }

    override fun setMenuMainEnabled(isEnabled: Boolean) {
        super.setMenuMainEnabled(isEnabled)
        isEmptyTrashVisible = isEnabled
        emptyTrashItem?.isVisible = isEmptyTrashVisible
        searchItem?.isVisible = isEnabled
    }

    private fun showMenu() {
        if (cloudPresenter.isSelectionMode) {
            deleteItem?.isVisible = true
            restoreItem?.isVisible = true
            copyItem?.isVisible = false
        } else {
            setActionBarTitle("")
            emptyTrashItem?.let { item ->
                setMenuItemTint(requireContext(), item, lib.toolkit.base.R.color.colorPrimary)
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
            ContextBottomDialog.Buttons.RESTORE -> cloudPresenter.moveContext()
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

    override fun onRemoveItemFromFavorites() {}

    override fun onResume() {
        super.onResume()
        cloudPresenter.isTrashMode = true
    }

    override fun onPause() {
        super.onPause()
        cloudPresenter.isTrashMode = false
    }

    override val section: Int
        get() = ApiContract.SectionType.CLOUD_TRASH

    companion object {
        val ID = CloudFileProvider.Section.Trash.path

        fun newInstance(account: String?): DocsTrashFragment {
            return DocsTrashFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_ACCOUNT, account)
                }
            }
        }
    }
}