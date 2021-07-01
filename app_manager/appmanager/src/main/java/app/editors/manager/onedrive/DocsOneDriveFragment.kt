package app.editors.manager.onedrive

import android.os.Bundle
import android.view.View
import app.documents.core.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import moxy.presenter.InjectPresenter

class DocsOneDriveFragment : DocsBaseFragment(), ActionButtonFragment, DocsOneDriveView {

    companion object {
        val TAG = DocsOneDriveFragment::class.java.simpleName

        const val KEY_ACCOUNT = "KEY_ACCOUNT"

        fun newInstance(account: String): DocsOneDriveFragment {
            return DocsOneDriveFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_ACCOUNT, account)
                }
            }
        }
    }


    var account: CloudAccount? = null

    @InjectPresenter
    lateinit var presenter: DocsOneDrivePresenter

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        presenter.checkBackStack()
    }

    override fun onRemoveItemFromFavorites() {

    }

    override fun isWebDav(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getPresenter(): DocsBasePresenter<out DocsBaseView> {
        return presenter
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        loadFiles()
        mSwipeRefresh.isRefreshing = true
    }

    private fun loadFiles() {
        presenter.getProvider()
    }

}