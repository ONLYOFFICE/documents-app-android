package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.View
import app.documents.core.network.ApiContract
import app.editors.manager.managers.providers.CloudFileProvider

class DocsCommonFragment : DocsCloudFragment() {

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

    override fun onScrollPage() {
        super.onScrollPage()
        if (cloudPresenter.stack == null) {
            cloudPresenter.getItemsById(ID)
        }
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        swipeRefreshLayout?.isRefreshing = true
        cloudPresenter.getItemsById(ID)
    }

    override fun onRemoveItemFromFavorites() {}

    private fun init() {
        cloudPresenter.checkBackStack()
    }

    override val section
        get() = ApiContract.SectionType.CLOUD_COMMON

    companion object {
        val ID = CloudFileProvider.Section.Common.path

        fun newInstance(account: String): DocsCommonFragment {
            return DocsCommonFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_ACCOUNT, account)
                }
            }
        }
    }
}