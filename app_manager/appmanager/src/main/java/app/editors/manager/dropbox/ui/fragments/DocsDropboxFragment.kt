package app.editors.manager.dropbox.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.dropbox.mvp.presenters.DocsDropboxPresenter
import app.editors.manager.dropbox.mvp.views.DocsDropboxView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import lib.toolkit.base.managers.utils.UiUtils
import moxy.presenter.InjectPresenter

class DocsDropboxFragment: DocsBaseFragment(), ActionButtonFragment, DocsDropboxView {

    companion object {
        val TAG = DocsDropboxFragment::class.java.simpleName

        const val KEY_ACCOUNT = "KEY_ACCOUNT"
        const val KEY_UPLOAD = "KEY_UPLOAD"
        const val KEY_UPDATE = "KEY_UPDATE"

        const val KEY_MODIFIED = "EXTRA_IS_MODIFIED"

        fun newInstance(account: String) = DocsDropboxFragment().apply {
            arguments = Bundle(1).apply {
                putString(KEY_ACCOUNT, account)
            }
        }
    }

    var account: CloudAccount? = null

    @InjectPresenter
    override lateinit var presenter: DocsDropboxPresenter

    override val isWebDav: Boolean
        get() = false

    private var activity: IMainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activity = context as IMainActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsDropboxFragment::class.java.simpleName + " - must implement - " +
                        IMainActivity::class.java.simpleName
            )
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

    override fun setVisibilityActionButton(isShow: Boolean) {
        activity?.showActionButton(isShow)
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
                UiUtils.setMenuItemTint(
                    requireContext(),
                    this,
                    lib.toolkit.base.R.color.colorPrimary
                )
                isVisible = true
            }
            moveItem = menu?.findItem(R.id.toolbar_selection_move)?.setVisible(true)
            copyItem = menu?.findItem(R.id.toolbar_selection_copy)?.setVisible(true)
            downloadItem = menu?.findItem(R.id.toolbar_selection_download)?.setVisible(false)
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
                //presenter.refreshToken()
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

    override fun onRemoveItemFromFavorites() {
        TODO("Not yet implemented")
    }

}