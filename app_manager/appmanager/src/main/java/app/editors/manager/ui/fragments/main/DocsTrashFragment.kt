package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
            if (emptyTrashItem != null) {
                emptyTrashItem!!.isVisible = isEmptyTrashVisible
            }
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
            mCloudPresenter.getItemsById(ID)
            return true
        }
        return false
    }

    override fun onScrollPage() {
        super.onScrollPage()
        mCloudPresenter.isTrashMode = true
        mCloudPresenter.getItemsById(ID)
        initViews()
    }

    override fun onClick(v: View) {
        mCloudPresenter.moveContext()
    }

    override fun onItemContextClick(view: View, position: Int) {
        val item = mExplorerAdapter.getItem(position) as Item
        mCloudPresenter.onContextClick(item, position, true)
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        if (mSwipeRefresh != null) {
            mSwipeRefresh.isRefreshing = true
        }
        mCloudPresenter.getItemsById(ID)
    }

    override fun setMenuMainEnabled(isEnabled: Boolean) {
        super.setMenuMainEnabled(isEnabled)
        if (emptyTrashItem != null) {
            isEmptyTrashVisible = isEnabled
            emptyTrashItem!!.isVisible = isEmptyTrashVisible
        }
        if (mSearchItem != null) {
            mSearchItem.isVisible = isEnabled
        }
    }

    override fun isActivePage(): Boolean {
        return true
    }


    private fun showMenu() {
        if (mCloudPresenter.isSelectionMode) {
            mDeleteItem.isVisible = true
            mMoveItem.isVisible = true
            mCopyItem.isVisible = false
        } else {
            setActionBarTitle("")
            setMenuItemTint(requireContext(), emptyTrashItem!!, R.color.colorWhite)
        }
    }

    private fun initViews() {
        if (mRecyclerView != null) {
            mRecyclerView.setPadding(
                mRecyclerView.paddingLeft, mRecyclerView.paddingTop,
                mRecyclerView.paddingLeft, 0
            )
            mCloudPresenter.isTrashMode = true
            mCloudPresenter.checkBackStack()
        }
    }

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons) {
        when (buttons) {
            ContextBottomDialog.Buttons.DELETE -> showQuestionDialog(
                getString(R.string.trash_popup_delete),
                mCloudPresenter.itemTitle,
                getString(R.string.dialogs_question_accept_delete),
                getString(R.string.dialogs_common_cancel_button),
                DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT
            )
            ContextBottomDialog.Buttons.MOVE -> mCloudPresenter.moveContext()
        }
        mContextBottomDialog.dismiss()
    }

    override fun onDeleteBatch(list: List<Entity>) {
        if (list.isEmpty()) {
            setMenuMainEnabled(false)
        }
        super.onDeleteBatch(list)
    }

    override fun onRemoveItemFromFavorites() {}

    companion object {
        fun newInstance(account: String?): DocsTrashFragment {
            return DocsTrashFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_ACCOUNT, account)
                }
            }
        }

        val ID = CloudFileProvider.Section.Trash.path
    }
}