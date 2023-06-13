package app.editors.manager.ui.fragments.base

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Current
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.mvp.presenters.main.OpenState
import app.editors.manager.mvp.presenters.storages.BaseStorageDocsPresenter
import app.editors.manager.mvp.views.base.BaseStorageDocsView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.fragments.storages.DocsDropboxFragment
import app.editors.manager.ui.popup.MainPopupItem
import app.editors.manager.ui.popup.SelectPopupItem
import lib.toolkit.base.managers.utils.FolderChooser
import lib.toolkit.base.managers.utils.PathUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.UiUtils
import java.io.File

abstract class BaseStorageDocsFragment: DocsBaseFragment(), ActionButtonFragment, BaseStorageDocsView {

    companion object {
        const val KEY_ACCOUNT = "KEY_ACCOUNT"
        const val KEY_UPLOAD = "KEY_UPLOAD"
        const val KEY_UPDATE = "KEY_UPDATE"
        const val KEY_CREATE = "KEY_CREATE"
        const val KEY_MODIFIED = "EXTRA_IS_MODIFIED"
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

    override fun onContextButtonClick(contextItem: ExplorerContextItem) {
        when (contextItem) {
            ExplorerContextItem.ExternalLink -> presenter.externalLink
            ExplorerContextItem.Edit -> presenter.getFileInfo()
            else -> super.onContextButtonClick(contextItem)
        }
        contextBottomDialog?.dismiss()
    }

    override fun onOpenLocalFile(file: CloudFile) {
        val uri = Uri.parse(file.webUrl)
        when(StringUtils.getExtension(file.fileExst)) {
            StringUtils.Extension.IMAGE -> {
                val state = OpenState.Media(getMediaFile(uri), false)
                showMediaActivity(state.explorer, state.isWebDav) {
                    // Stub
                }
            }
            else -> super.onOpenLocalFile(file)
        }

    }

    private fun getMediaFile(uri: Uri): Explorer =
        Explorer().apply {
            val file = File(context?.let { PathUtils.getPath(it, uri).toString() }.toString())
            val explorerFile = CloudFile().apply {
                pureContentLength = file.length()
                webUrl = file.absolutePath
                fileExst = StringUtils.getExtensionFromPath(file.name)
                title = file.name
                isClicked = true
            }
            current = Current().apply {
                title = file.name
                filesCount = "1"
            }
            files = mutableListOf(explorerFile)
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
            setAccountEnable(false)
        }
    }

    override fun onStateMenuDefault(sortBy: String, isAsc: Boolean) {
        super.onStateMenuDefault(sortBy, isAsc)
        searchCloseButton?.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onFileWebView(file: CloudFile) {
        activity?.showWebViewer(file)
    }

    override fun onChooseDownloadFolder() {
        FolderChooser(requireActivity().activityResultRegistry, { data ->
            data?.let { presenter.download(it) }
        }).show()
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

    override fun showMainActionPopup(vararg excluded: MainPopupItem) {
        super.showMainActionPopup(MainPopupItem.SortBy.Type, MainPopupItem.SortBy.Author)
    }

    override fun showSelectActionPopup(vararg excluded: SelectPopupItem) {
        super.showSelectActionPopup(SelectPopupItem.Operation.Restore)
    }

    override fun onFileUploadPermission() {
        showMultipleFilePickerActivity { uris ->
            if (!uris.isNullOrEmpty()) {
                presenter.upload(
                    null,
                    uris,
                    KEY_UPLOAD)
            }
        }
    }

    abstract fun getDocsPresenter(): BaseStorageDocsPresenter<out BaseStorageDocsView>

}