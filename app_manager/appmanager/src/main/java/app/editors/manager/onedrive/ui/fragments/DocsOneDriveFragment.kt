package app.editors.manager.onedrive.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.onedrive.mvp.presenters.DocsOneDrivePresenter
import app.editors.manager.onedrive.mvp.views.DocsOneDriveView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.fragments.main.DocsBaseFragment
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
    override lateinit var presenter: DocsOneDrivePresenter

    override val isWebDav: Boolean?
        get() = false

    private var activity: IMainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activity = context as IMainActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsOneDriveFragment::class.java.simpleName + " - must implement - " +
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
                BaseActivity.REQUEST_ACTIVITY_OPERATION -> {
                    onRefresh()
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
        menu?.let {
            deleteItem?.isEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
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
            else -> { }
        }
    }

    override fun onUpdateItemFavorites() { }

    override fun setVisibilityActionButton(isShow: Boolean) {
        activity?.showActionButton(isShow)
    }

    override fun onStateMenuDefault(sortBy: String, isAsc: Boolean) {
        super.onStateMenuDefault(sortBy, isAsc)
        menu?.findItem(R.id.toolbar_sort_item_type)?.isVisible = false
        menu?.findItem(R.id.toolbar_sort_item_owner)?.isVisible = false
        searchCloseButton?.setOnClickListener { v: View? ->
            onBackPressed()
        }
    }

    override fun onStateMenuSelection() {
        super.onStateMenuSelection()
        if (menu != null && menuInflater != null) {
            menuInflater?.inflate(R.menu.docs_select, menu)
            deleteItem = menu?.findItem(R.id.toolbar_selection_delete)?.apply {
                setMenuItemTint(requireContext(), this, lib.toolkit.base.R.color.colorPrimary)
                isVisible = true
            }
            moveItem = menu?.findItem(R.id.toolbar_selection_move)?.setVisible(true)
            copyItem = menu?.findItem(R.id.toolbar_selection_copy)?.setVisible(true)
            downloadItem = menu?.findItem(R.id.toolbar_selection_download)?.setVisible(false)
            restoreItem = menu?.findItem(R.id.toolbar_selection_restore)?.setVisible(false)
            setAccountEnable(false)
        }
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        loadFiles()
        swipeRefreshLayout?.isRefreshing = true
    }

    override fun onError(message: String?) {
        when(message) {
            context?.getString(R.string.errors_client_unauthorized) -> {
                presenter.refreshToken()
            }
            else -> {
                message?.let { showSnackBar(it) }
            }
        }

    }

    private fun init() {
        presenter.checkBackStack()
    }

    private fun loadFiles() {
        presenter.getProvider()
    }

}