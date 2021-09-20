package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import app.editors.manager.R
import app.editors.manager.managers.providers.CloudFileProvider
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs

class DocsShareFragment : DocsCloudFragment() {

    companion object {
        val ID = CloudFileProvider.Section.Shared.path

        fun newInstance(account: String): DocsShareFragment {
            return DocsShareFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_ACCOUNT, account)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onSwipeRefresh(): Boolean {
        if (!super.onSwipeRefresh()) {
            mCloudPresenter.getItemsById(ID)
            return true
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        if (mCloudPresenter.isSelectionMode) {
            val mShareDeleteItem = menu.findItem(R.id.toolbar_selection_share_delete)
            mShareDeleteItem.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toolbar_selection_share_delete) {
            mCloudPresenter.removeShare()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onScrollPage() {
        super.onScrollPage()
        if (mCloudPresenter.stack == null) {
            mCloudPresenter.getItemsById(ID)
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            if (DocsBasePresenter.TAG_DIALOG_ACTION_REMOVE_SHARE == tag) {
                mCloudPresenter.removeShareSelected()
            }
        }
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        if (mSwipeRefresh != null) {
            mSwipeRefresh.isRefreshing = true
        }
        mCloudPresenter.getItemsById(ID)
    }

    override fun onRemoveItemFromFavorites() {}
    private fun init() {
        mCloudPresenter.checkBackStack()
    }

}