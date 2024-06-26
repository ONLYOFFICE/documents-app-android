package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.main.DocsWebDavPresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.mvp.views.main.DocsWebDavView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.ActionBottomDialog
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

open class DocsWebDavFragment : DocsBaseFragment(), DocsWebDavView, ActionButtonFragment {

    protected var provider: WebdavProvider? = null

    @InjectPresenter
    lateinit var webDavPresenter: DocsWebDavPresenter

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            if (isActivePage && (requestCode == BaseActivity.REQUEST_ACTIVITY_MEDIA ||
                        requestCode == REQUEST_PDF)
            ) {
                webDavPresenter.deleteTempFile()
            }
        } else if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BaseActivity.REQUEST_ACTIVITY_FILE_PICKER -> {
//                    webDavPresenter.upload(data!!.data, data.clipData)
                }
                BaseActivity.REQUEST_ACTIVITY_CAMERA -> {
                    webDavPresenter.upload(cameraUri, null)
                }
                REQUEST_PRESENTATION, REQUEST_PDF, REQUEST_DOCS, REQUEST_SHEETS -> {
                    if (data?.data != null) {
                        if (data.getBooleanExtra("EXTRA_IS_MODIFIED", false)) {
                            webDavPresenter.upload(data.data, null)
                        }
                    }
                }
            }
        }
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

    override fun setExpandToolbar() {
//        if (requireActivity() instanceof MainActivity) {
//            ((MainActivity) requireActivity()).expandToolBar();
//        }
    }

    override fun onFileMedia(explorer: Explorer, isWebDAv: Boolean) {
        showMediaActivity(explorer, isWebDAv) {
            webDavPresenter.deleteTempFile()
        }
    }

    override fun setVisibilityActionButton(isShow: Boolean) {
        mainActivity?.showActionButton(isShow)
    }


    override val presenter: DocsBasePresenter<out DocsBaseView>
        get() = webDavPresenter

    override val isWebDav: Boolean?
        get() = true

    private fun loadFiles() {
        webDavPresenter.getProvider()
    }

    private fun init() {
        webDavPresenter.checkBackStack()
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