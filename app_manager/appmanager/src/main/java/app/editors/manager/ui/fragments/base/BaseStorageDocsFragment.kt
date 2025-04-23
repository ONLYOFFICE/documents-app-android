package app.editors.manager.ui.fragments.base

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import app.documents.core.model.cloud.Access
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Current
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.mvp.presenters.storages.BaseStorageDocsPresenter
import app.editors.manager.mvp.views.base.BaseStorageDocsView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.fragments.storages.DocsDropboxFragment
import lib.toolkit.base.managers.utils.EditType
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

    abstract val storagePresenter: BaseStorageDocsPresenter<*, *>

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
        storagePresenter.getItems()
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
            is ExplorerContextItem.ExternalLink -> storagePresenter.externalLink
            else -> super.onContextButtonClick(contextItem)
        }
    }

    override fun onOpenLocalFile(file: CloudFile, editType: EditType, access: Access) {
        val uri = file.webUrl.toUri()
        when(StringUtils.getExtension(file.fileExst)) {
            StringUtils.Extension.IMAGE -> {
                showMediaActivity(getMediaFile(uri), false)
            }
            else -> super.onOpenLocalFile(file, editType, access)
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
                storagePresenter.refreshToken()
            }
            else -> {
                message?.let { showSnackBar(it) }
            }
        }

    }

    override fun onFileUploadPermission(extension: String?) {
        showMultipleFilePickerActivity(extension) { uris ->
            if (!uris.isNullOrEmpty()) {
                presenter.upload(
                    null,
                    uris,
                    KEY_UPLOAD
                )
            }
        }
    }
}