package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.View
import app.editors.manager.R
import app.editors.manager.managers.providers.CloudFileProvider

class DocsProjectsFragment : DocsCloudFragment() {

    companion object {

        fun newInstance(fileData: String? = null): DocsProjectsFragment {
            return DocsProjectsFragment().apply {
                arguments = Bundle().apply {
                    putString(MainPagerFragment.KEY_FILE_DATA, fileData)
                }
            }
        }

        val ID = CloudFileProvider.Section.Projects.path
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onSwipeRefresh(): Boolean {
        if (!super.onSwipeRefresh()) {
            presenter.getItemsById(ID)
            return true
        }
        return false
    }

    override fun onScrollPage() {
        super.onScrollPage()
        if (presenter.stack == null) {
            presenter.getItemsById(ID)
        }
    }

    override fun onError(message: String?) {
        if (message != null) {
            if (message != getString(R.string.errors_server_error) + "500") {
                super.onError(message)
            }
        }
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        if (mSwipeRefresh != null) {
            mSwipeRefresh.isRefreshing = true
        }
        presenter.getItemsById(ID)
        arguments?.getString(MainPagerFragment.KEY_FILE_DATA)?.let {
            mCloudPresenter.openFile(it)
        }
    }

    override fun onRemoveItemFromFavorites() {}

    private fun init() {
        presenter.checkBackStack()
    }

    fun setFileData(fileData: String) {
        mCloudPresenter.openFile(fileData)
    }
}