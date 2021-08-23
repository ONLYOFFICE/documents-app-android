package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.View
import app.documents.core.network.ApiContract
import app.editors.manager.managers.providers.CloudFileProvider

class DocsFavoritesFragment : DocsCloudFragment() {

    companion object {
        val ID = CloudFileProvider.Section.Favorites.path

        fun newInstance(account: String): DocsFavoritesFragment {
            return DocsFavoritesFragment().apply {
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

    override fun onScrollPage() {
        super.onScrollPage()
        if (mCloudPresenter.stack == null) {
            mCloudPresenter.getItemsById(ID)
        }
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        if (mSwipeRefresh != null) {
            mSwipeRefresh.isRefreshing = true
        }
        mCloudPresenter.getItemsById(ID)
    }

    override fun onRemoveItemFromFavorites() {
        mCloudPresenter.removeFromFavorites()
    }

    private fun init() {
        mCloudPresenter.checkBackStack()
    }

    override fun getSection() = ApiContract.SectionType.UNKNOWN
}