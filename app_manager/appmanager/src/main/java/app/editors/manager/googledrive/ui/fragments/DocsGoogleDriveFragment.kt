package app.editors.manager.googledrive.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.googledrive.mvp.presenters.DocsGoogleDrivePresenter
import app.editors.manager.googledrive.mvp.views.DocsGoogleDriveView
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.onedrive.ui.fragments.DocsOneDriveFragment
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.fragments.main.DocsOnDeviceFragment
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

open class DocsGoogleDriveFragment: DocsBaseFragment(), ActionButtonFragment, DocsGoogleDriveView {

    companion object {
        val TAG = DocsGoogleDriveFragment::class.java.simpleName

        const val KEY_UPLOAD = "KEY_UPLOAD"
        const val KEY_UPDATE = "KEY_UPDATE"
        const val KEY_MODIFIED = "EXTRA_IS_MODIFIED"

        fun newInstance(): DocsGoogleDriveFragment = DocsGoogleDriveFragment()

    }

    @InjectPresenter
    override lateinit var presenter: DocsGoogleDrivePresenter


    override val isWebDav: Boolean?
        get() = false

    override fun onRemoveItemFromFavorites() {
        TODO("Not yet implemented")
    }

    private var activity: IMainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activity = context as IMainActivity
            App.getApp().appComponent.inject(this)
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsOneDriveFragment::class.java.simpleName + " - must implement - " +
                        IMainActivity::class.java.simpleName
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_MULTIPLE_FILES_DOWNLOAD -> {
                    data?.data?.let { presenter.download(it) }
                }
                BaseActivity.REQUEST_ACTIVITY_OPERATION -> {
                    onRefresh()
                }
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
                        KEY_UPLOAD
                    )
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

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        when (buttons) {
            ContextBottomDialog.Buttons.MOVE -> presenter.moveContext()
            ContextBottomDialog.Buttons.COPY -> presenter.copy()
            ContextBottomDialog.Buttons.DOWNLOAD -> onFileDownloadPermission()
            ContextBottomDialog.Buttons.RENAME -> if (presenter.itemClicked is CloudFile) {
                showEditDialogRename(
                    getString(R.string.dialogs_edit_rename_title),
                    StringUtils.getNameWithoutExtension(presenter.itemTitle),
                    getString(R.string.dialogs_edit_hint),
                    DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                    getString(R.string.dialogs_edit_accept_rename),
                    getString(R.string.dialogs_common_cancel_button))
            } else {
                showEditDialogRename(
                    getString(R.string.dialogs_edit_rename_title),
                    presenter.itemTitle,
                    getString(R.string.dialogs_edit_hint),
                    DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                    getString(R.string.dialogs_edit_accept_rename),
                    getString(R.string.dialogs_common_cancel_button))
            }
            ContextBottomDialog.Buttons.DELETE -> showQuestionDialog(
                getString(R.string.dialogs_question_delete),
                presenter.itemTitle,
                getString(R.string.dialogs_question_accept_remove),
                getString(R.string.dialogs_common_cancel_button),
                DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT
            )
        }
    }

    override fun onDocsBatchOperation() {
        onDialogClose()
        onSnackBar(getString(R.string.operation_complete_message))
        onRefresh()
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

    override fun setVisibilityActionButton(isShow: Boolean) {
        activity?.showActionButton(isShow)
    }

    override fun onStateMenuDefault(sortBy: String, isAsc: Boolean) {
        super.onStateMenuDefault(sortBy, isAsc)
        menu?.findItem(R.id.toolbar_sort_item_type)?.isVisible = false
        menu?.findItem(R.id.toolbar_sort_item_owner)?.isVisible = false
        menu?.findItem(R.id.toolbar_sort_item_size)?.isVisible = false
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
            downloadItem = menu?.findItem(R.id.toolbar_selection_download)?.setVisible(true)
            restoreItem = menu?.findItem(R.id.toolbar_selection_restore)?.setVisible(false)
            setAccountEnable(false)
        }
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        loadFiles()
        swipeRefreshLayout?.isRefreshing = true
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
}