package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.View
import app.documents.core.network.ApiContract
import app.editors.manager.managers.providers.CloudFileProvider

class DocsMyFragment : DocsCloudFragment() {

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

    override val section: Int
        get() = ApiContract.SectionType.CLOUD_USER

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        swipeRefreshLayout?.isRefreshing = true
        cloudPresenter.getItemsById(ID)
    }

    override fun onUpdateItemFavorites() {
        explorerAdapter?.updateItem(cloudPresenter.itemClicked)
    }

    private fun init() {
        explorerAdapter?.isSectionMy = true
        cloudPresenter.checkBackStack()
    }

    companion object {
        val ID = CloudFileProvider.Section.My.path

        fun newInstance(stringAccount: String): DocsMyFragment {
            return DocsMyFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_ACCOUNT, stringAccount)
                }
            }
        }
    }
}