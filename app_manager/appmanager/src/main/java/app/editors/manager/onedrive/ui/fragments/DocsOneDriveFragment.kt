package app.editors.manager.onedrive.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.Constants
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.mvp.presenters.DocsOneDrivePresenter
import app.editors.manager.onedrive.mvp.views.DocsOneDriveView
import app.editors.manager.onedrive.ui.fragments.OneDriveSignInFragment.Companion.TAG
import app.editors.manager.onedrive.ui.fragments.OneDriveSignInFragment.Companion.newInstance
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.fragments.main.DocsOnDeviceFragment
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

open class DocsOneDriveFragment : DocsBaseFragment(), ActionButtonFragment, DocsOneDriveView {

    companion object {
        val TAG = DocsOneDriveFragment::class.java.simpleName

        const val KEY_ACCOUNT = "KEY_ACCOUNT"
        const val KEY_UPLOAD = "KEY_UPLOAD"
        const val KEY_UPDATE = "KEY_UPDATE"

        const val KEY_MODIFIED = "EXTRA_IS_MODIFIED"

        fun newInstance(account: String) = DocsOneDriveFragment().apply {
            arguments = Bundle(1).apply {
                putString(KEY_ACCOUNT, account)
            }
        }
    }


    var account: CloudAccount? = null

    @InjectPresenter
    lateinit var presenter: DocsOneDrivePresenter
    private var activity: IMainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activity = context as IMainActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsOnDeviceFragment::class.java.simpleName + " - must implement - " +
                        IMainActivity::class.java.simpleName
            )
        }
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isActivePage && resultCode == Activity.RESULT_CANCELED) {
            onRefresh()
        } else if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_DOCS, REQUEST_SHEETS, REQUEST_PRESENTATION -> data?.data?.let {
                    if(data.getBooleanExtra(KEY_MODIFIED, false)) {
                        presenter.upload(
                            it,
                            null,
                            KEY_UPDATE
                        )
                    }
                }
                BaseActivity.REQUEST_ACTIVITY_FILE_PICKER -> data?.clipData?.let {
                    presenter.upload(
                        null,
                        it,
                        KEY_UPLOAD)
                }.run {
                    presenter.upload(data?.data, null, KEY_UPLOAD)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.showAccount(false)
        activity?.showNavigationButton(false)
    }

    override fun setToolbarState(isVisible: Boolean) {
        activity?.showAccount(isVisible)
        activity?.showNavigationButton(!isVisible)
    }


    override fun onActionBarTitle(title: String) {
        if (isActivePage) {
            setActionBarTitle(title)
            if (title == "0") {
                disableMenu()
            }
        }
    }

    private fun disableMenu() {
        if (mMenu != null) {
            mDeleteItem.isEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        presenter.checkBackStack()
    }


    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        super.onContextButtonClick(buttons)
        when(buttons) {
            ContextBottomDialog.Buttons.EXTERNAL -> {
                presenter.externalLink
            }
            ContextBottomDialog.Buttons.EDIT -> {
                presenter.getFileInfo()
            }
        }
    }

    override fun onRemoveItemFromFavorites() {
        //stub
    }

    override fun isWebDav(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setVisibilityActionButton(isShow: Boolean) {
        activity?.showActionButton(isShow)
    }

    override fun getPresenter(): DocsBasePresenter<out DocsBaseView> {
        return presenter
    }


    override fun onStateMenuDefault(sortBy: String, isAsc: Boolean) {
        super.onStateMenuDefault(sortBy, isAsc)
        mMenu?.findItem(R.id.toolbar_sort_item_type)?.isVisible = false
        mMenu?.findItem(R.id.toolbar_sort_item_owner)?.isVisible = false
        mSearchCloseButton.setOnClickListener { v: View? ->
            onBackPressed()
        }
    }

    override fun onStateMenuSelection() {
        super.onStateMenuSelection()
        if (mMenu != null && mMenuInflater != null) {
            mMenuInflater?.inflate(R.menu.docs_select, mMenu)
            mDeleteItem = mMenu?.findItem(R.id.toolbar_selection_delete)?.setVisible(true)
            mMoveItem = mMenu?.findItem(R.id.toolbar_selection_move)?.setVisible(true)
            mCopyItem = mMenu?.findItem(R.id.toolbar_selection_copy)?.setVisible(true)
            mDownloadItem = mMenu?.findItem(R.id.toolbar_selection_download)?.setVisible(false)
            setMenuItemTint(requireContext(), mDeleteItem, R.color.colorPrimary)
            setAccountEnable(false)
        }
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        loadFiles()
        mSwipeRefresh.isRefreshing = true
    }

    private fun loadFiles() {
        presenter.getProvider()
    }

    override fun onError(message: String?) {
        when(message) {
            context?.getString(R.string.errors_client_unauthorized) -> {
                val storage = Storage(
                    OneDriveUtils.ONEDRIVE_STORAGE,
                    Constants.OneDrive.COM_CLIENT_ID,
                    Constants.OneDrive.COM_REDIRECT_URL
                )
                showFragment(newInstance(storage), OneDriveSignInFragment.TAG, false)
            }
            else -> {
                message?.let { showSnackBar(it) }
            }
        }

    }

}