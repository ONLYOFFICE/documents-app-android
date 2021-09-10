package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.managers.providers.CloudFileProvider
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs

class DocsShareFragment : DocsCloudFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onSwipeRefresh(): Boolean {
        if (!super.onSwipeRefresh()) {
            cloudPresenter.getItemsById(ID)
            return true
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        if (cloudPresenter.isSelectionMode) {
            val mShareDeleteItem = menu.findItem(R.id.toolbar_selection_share_delete)
            mShareDeleteItem.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toolbar_selection_share_delete) {
            cloudPresenter.removeShare()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onScrollPage() {
        super.onScrollPage()
        cloudPresenter.stack?.let {
            cloudPresenter.getItemsById(ID)
        }
    }


    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        tag?.let {
            if (DocsBasePresenter.TAG_DIALOG_ACTION_REMOVE_SHARE == tag) {
                cloudPresenter.removeShareSelected()
            }
        }
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        swipeRefreshLayout?.isRefreshing = true
        cloudPresenter.getItemsById(ID)
    }

    override fun onRemoveItemFromFavorites() { }

    private fun init() {
        cloudPresenter.checkBackStack()
    }

    override val section: Int
        get() = ApiContract.SectionType.CLOUD_SHARE

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
}