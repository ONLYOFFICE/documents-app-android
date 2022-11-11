package app.editors.manager.storages.base.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Current
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.presenters.main.OpenState
import app.editors.manager.storages.base.presenter.BaseStorageDocsPresenter
import app.editors.manager.storages.base.view.BaseStorageDocsView
import app.editors.manager.storages.dropbox.ui.fragments.DocsDropboxFragment
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MediaActivity
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.popup.MainActionBarPopup
import app.editors.manager.ui.popup.SelectActionBarPopup
import lib.toolkit.base.managers.utils.PathUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.popup.ActionBarPopupItem
import java.io.File

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

    override fun onOpenLocalFile(file: CloudFile) {
        val uri = Uri.parse(file.webUrl)
        when(StringUtils.getExtension(file.fileExst)) {
            StringUtils.Extension.IMAGE -> {
                val state = OpenState.Media(getMediaFile(uri), false)
                MediaActivity.show(this, state.explorer, state.isWebDav)
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
            files = listOf(explorerFile)
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

    override fun onActionButtonClick(buttons: ActionBottomDialog.Buttons?) {
        when (buttons) {
            ActionBottomDialog.Buttons.PHOTO -> if (checkCameraPermission()) {
                showCameraActivity(TimeUtils.fileTimeStamp)
            }
            else -> {
                super.onActionButtonClick(buttons)
            }
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
        searchCloseButton?.setOnClickListener {
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

    override fun showMainActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showMainActionBarMenu(listOf(MainActionBarPopup.Type, MainActionBarPopup.Author))
    }

    override fun showSelectedActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showSelectedActionBarMenu(listOf(SelectActionBarPopup.Restore))
    }

    abstract fun getDocsPresenter(): BaseStorageDocsPresenter<out BaseStorageDocsView>

}