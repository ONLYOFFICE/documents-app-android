package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.main.DocsWebDavPresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.mvp.views.main.DocsWebDavView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.ActionBottomDialog
import lib.toolkit.base.managers.utils.TimeUtils.fileTimeStamp
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

open class DocsWebDavFragment : DocsBaseFragment(), DocsWebDavView, ActionButtonFragment {

    companion object {
        val TAG: String = DocsWebDavFragment::class.java.simpleName

        const val KEY_PROVIDER = "KEY_PROVIDER"

        fun newInstance(provider: WebDavApi.Providers): DocsWebDavFragment {
            return DocsWebDavFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_PROVIDER, provider)
                }
            }
        }
    }

    protected var provider: WebDavApi.Providers? = null

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
            provider = it.getSerializable(KEY_PROVIDER) as WebDavApi.Providers
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                BaseActivity.REQUEST_ACTIVITY_OPERATION -> {
                    webDavPresenter.checkBackStack()
                }
                BaseActivity.REQUEST_ACTIVITY_FILE_PICKER -> {
                    webDavPresenter.upload(data!!.data, data.clipData)
                }
                BaseActivity.REQUEST_ACTIVITY_CAMERA -> {
                    webDavPresenter.upload(mCameraUri, null)
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

    override fun onActionButtonClick(buttons: ActionBottomDialog.Buttons) {
        super.onActionButtonClick(buttons)
        if (buttons == ActionBottomDialog.Buttons.PHOTO) {
            if (checkCameraPermission()) {
                showCameraActivity(fileTimeStamp)
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
        super.onStateEmptyBackStack()
        loadFiles()
        mSwipeRefresh.isRefreshing = true
    }

    private fun loadFiles() {
        webDavPresenter.getProvider()
    }

    private fun init() {
        webDavPresenter.checkBackStack()
        getArgs()
    }

    override fun onStateMenuDefault(sortBy: String, isAsc: Boolean) {
        super.onStateMenuDefault(sortBy, isAsc)
        mMenu?.let {
            it.findItem(R.id.toolbar_sort_item_owner).isVisible = false
        }
    }

    override fun onStateMenuSelection() {
        mMenu?.let { menu ->
            mMenuInflater?.inflate(R.menu.docs_select, mMenu)
            mDeleteItem = menu.findItem(R.id.toolbar_selection_delete).setVisible(true)
            mMoveItem = menu.findItem(R.id.toolbar_selection_move).setVisible(true)
            mCopyItem = menu.findItem(R.id.toolbar_selection_copy).setVisible(true)
            mDownloadItem = menu.findItem(R.id.toolbar_selection_download).setVisible(true)
            setMenuItemTint(requireContext(), mDeleteItem, R.color.colorWhite)
            mainActivity?.showAccount(false)
        }
    }

    override fun onListEnd() {}

    override fun onActionBarTitle(title: String) {
        setActionBarTitle(title)
    }

    override fun onRemoveItemFromFavorites() {
        //stub
    }

    override fun onActionDialog() {
        mActionBottomDialog.setLocal(true)
        mActionBottomDialog.setWebDav(true)
        mActionBottomDialog.setOnClickListener(this)
        mActionBottomDialog.show(requireFragmentManager(), ActionBottomDialog.TAG)
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

    override fun setVisibilityActionButton(isShow: Boolean) {
        mainActivity?.showActionButton(isShow)
    }

    override fun isActivePage(): Boolean {
        return true
    }

    override fun getPresenter(): DocsBasePresenter<out DocsBaseView?> {
        return webDavPresenter
    }

    override fun isWebDav(): Boolean {
        return true
    }


}