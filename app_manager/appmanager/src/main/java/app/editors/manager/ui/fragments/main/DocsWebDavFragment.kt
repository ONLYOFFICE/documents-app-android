package app.editors.manager.ui.fragments.main

import android.content.Context
import android.os.Bundle
import android.view.View
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.mvp.presenters.main.DocsWebDavPresenter
import app.editors.manager.mvp.views.main.DocsWebDavView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.ActionBottomDialog
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint
import lib.toolkit.base.managers.utils.getSerializableExt
import moxy.presenter.InjectPresenter

open class DocsWebDavFragment : DocsBaseFragment(), DocsWebDavView, ActionButtonFragment {

    protected var provider: WebdavProvider? = null

    @InjectPresenter
    override lateinit var presenter: DocsWebDavPresenter

    private var mainActivity: IMainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IMainActivity) {
            mainActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            provider = it.getSerializableExt(KEY_PROVIDER, WebdavProvider::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.setSectionType(ApiContract.SectionType.WEB_DAV)
        init()
    }

    override fun onSwipeRefresh(): Boolean {
        if (!super.onSwipeRefresh()) {
            loadFiles()
            return true
        }
        return false
    }

    override fun onStateEmptyBackStack() {
        loadFiles()
        swipeRefreshLayout?.isRefreshing = true
    }

    override fun onStateMenuSelection() {
        menu?.let { menu ->
            menuInflater?.inflate(R.menu.docs_select, this.menu)
            deleteItem = menu.findItem(R.id.toolbar_selection_delete).apply {
                setMenuItemTint(requireContext(), this, lib.toolkit.base.R.color.colorPrimary)
                isVisible = true
            }
            mainActivity?.showAccount(false)
        }
    }

    override fun onListEnd() {}

    override fun onActionBarTitle(title: String) {
        setActionBarTitle(title)
    }

    override fun onActionDialog() {
        actionBottomDialog?.isLocal = true
        actionBottomDialog?.isWebDav = true
        actionBottomDialog?.onClickListener = this
        actionBottomDialog?.show(parentFragmentManager, ActionBottomDialog.TAG)
    }

    override fun setToolbarState(isVisible: Boolean) {
        mainActivity?.let {
            it.setAppBarStates(false)
            it.showNavigationButton(!isVisible)
            it.showAccount(isVisible)
        }
    }

    override fun onFileMedia(explorer: Explorer, isWebDav: Boolean) {
        showMediaActivity(explorer, isWebDav) {
            presenter.deleteTempFile()
        }
    }

    override fun setVisibilityActionButton(isShow: Boolean) {
        mainActivity?.showActionButton(isShow)
    }

    private fun loadFiles() {
        presenter.getItems()
    }

    private fun init() {
        presenter.checkBackStack()
    }

    companion object {
        val TAG: String = DocsWebDavFragment::class.java.simpleName

        const val KEY_PROVIDER = "KEY_PROVIDER"

        fun newInstance(provider: WebdavProvider): DocsWebDavFragment {
            return DocsWebDavFragment().apply {
                arguments = Bundle(1).apply {
                    putSerializable(KEY_PROVIDER, provider)
                }
            }
        }
    }
}