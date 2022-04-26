package app.editors.manager.storages.base.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.storages.base.presenter.BaseStorageDocsPresenter
import app.editors.manager.storages.base.view.BaseStorageDocsView
import app.editors.manager.storages.dropbox.ui.fragments.DocsDropboxFragment
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.activities.base.BaseActivity

abstract class BaseStorageDocsFragment: DocsBaseFragment(), ActionButtonFragment, BaseStorageDocsView {

    companion object {
        const val KEY_ACCOUNT = "KEY_ACCOUNT"
        const val KEY_UPLOAD = "KEY_UPLOAD"
        const val KEY_UPDATE = "KEY_UPDATE"
        const val KEY_CREATE = "KEY_CREATE"
        const val KEY_MODIFIED = "EXTRA_IS_MODIFIED"

        const val REQUEST_MULTIPLE_FILES_DOWNLOAD = 11000023
    }

    var account: CloudAccount? = null

    var activity: IMainActivity? = null


    override val isWebDav: Boolean
        get() = false

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }


    private fun init() {
        presenter.checkBackStack()
        activity?.showAccount(true)
    }

    private fun loadFiles() {
        presenter.getProvider()
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        loadFiles()
        swipeRefreshLayout?.isRefreshing = true
    }

    override fun setVisibilityActionButton(isShow: Boolean) {
        activity?.showActionButton(isShow)
    }

    private fun disableMenu() {
        menu?.let {
            deleteItem?.isEnabled = false
        }
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
            downloadItem = menu?.findItem(R.id.toolbar_selection_download)?.setVisible(true)
            restoreItem = menu?.findItem(R.id.toolbar_selection_restore)?.setVisible(false)
            setAccountEnable(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_MULTIPLE_FILES_DOWNLOAD -> {
                    data?.data?.let { presenter.download(it) }
                }
            }
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

    override fun onFileWebView(file: CloudFile) {
        showViewerActivity(file)
    }

    override fun onChooseDownloadFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_MULTIPLE_FILES_DOWNLOAD)
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

    override val presenter: BaseStorageDocsPresenter<out BaseStorageDocsView>
        get() = getDocsPresenter()

    override fun onUpdateItemFavorites() {
        TODO("Not yet implemented")
    }

    abstract fun getDocsPresenter(): BaseStorageDocsPresenter<out BaseStorageDocsView>
}