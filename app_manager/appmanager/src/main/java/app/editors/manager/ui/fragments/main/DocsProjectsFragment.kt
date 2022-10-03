package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.View
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.managers.providers.CloudFileProvider

class DocsProjectsFragment : DocsCloudFragment() {

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
        if (cloudPresenter.stack == null) {
            presenter.getItemsById(ID)
        }
    }

    override fun onError(message: String?) {
        message?.let {
            if (it != getString(R.string.errors_server_error) + "500" && it != "HTTP 500 Server error") {
                super.onError(message)
            }
        }
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        swipeRefreshLayout?.isRefreshing = true
        presenter.getItemsById(ID)
        arguments?.getString(MainPagerFragment.KEY_FILE_DATA)?.let {
            cloudPresenter.openFile(it)
        }
    }

    override val section: Int
        get() = ApiContract.SectionType.CLOUD_PROJECTS

    private fun init() {
        presenter.checkBackStack()
    }

    companion object {
        val ID = CloudFileProvider.Section.Projects.path

        fun newInstance(account: String, fileData: String? = null): DocsProjectsFragment {
            return DocsProjectsFragment().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_ACCOUNT, account)
                    putString(MainPagerFragment.KEY_FILE_DATA, fileData)
                }
            }
        }
    }
}