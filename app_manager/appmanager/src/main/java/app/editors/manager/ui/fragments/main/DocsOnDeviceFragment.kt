package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.main.DocsOnDevicePresenter
import app.editors.manager.mvp.presenters.main.OpenState
import app.editors.manager.mvp.views.main.DocsOnDeviceView
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MediaActivity
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.popup.MainActionBarPopup
import app.editors.manager.ui.popup.SelectActionBarPopup
import app.editors.manager.ui.views.custom.PlaceholderViews
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import lib.toolkit.base.ui.popup.ActionBarPopupItem
import moxy.presenter.InjectPresenter
import java.util.*

class DocsOnDeviceFragment : DocsBaseFragment(), DocsOnDeviceView, ActionButtonFragment {

    internal enum class Operation {
        COPY, MOVE
    }

    @InjectPresenter
    override lateinit var presenter: DocsOnDevicePresenter
    private var activity: IMainActivity? = null
    private var operation: Operation? = null
    private var preferenceTool: PreferenceTool? = null

    private val importFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { data: Uri? ->
        data?.let { presenter.import(it) }
    }

    private val openFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { data: Uri? ->
        data?.let { presenter.openFromChooser(it) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activity = context as IMainActivity
            preferenceTool = App.getApp().appComponent.preference
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsOnDeviceFragment::class.java.simpleName + " - must implement - " +
                        IMainActivity::class.java.simpleName
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BaseActivity.REQUEST_ACTIVITY_CAMERA -> {
                    presenter.refresh()
                }
                BaseActivity.REQUEST_SELECT_FOLDER -> {
                    if (operation != null && data != null && data.data != null) {
                        if (operation == Operation.MOVE) {
                            presenter.moveFile(data.data, false)
                        } else if (operation == Operation.COPY) {
                            presenter.moveFile(data.data, true)
                        }
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            when (requestCode) {
                BaseActivity.REQUEST_ACTIVITY_CAMERA -> presenter.deletePhoto()
                REQUEST_STORAGE_ACCESS -> {
                    preferenceTool?.isShowStorageAccess = false
                    presenter.recreateStack()
                    presenter.getItemsById(LocalContentTools.getDir(requireContext()))
                }
                REQUEST_STORAGE_IMPORT -> {
                    preferenceTool?.isShowStorageAccess = false
                    importFile.launch(arrayOf(ActivitiesUtils.PICKER_NO_FILTER))
                }
            }
        }
    }

    var uri: Uri? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoto()
            }
        } else if (requestCode == PERMISSION_READ_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                preferenceTool?.isShowStorageAccess = true
                checkStorage(TAG_STORAGE_IMPORT)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.setSectionType(ApiContract.SectionType.DEVICE_DOCUMENTS)
        checkStorage(TAG_STORAGE_ACCESS)
        init()
    }

    override fun onStateMenuDefault(sortBy: String, isAsc: Boolean) {
        super.onStateMenuDefault(sortBy, isAsc)
        openItem?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_item_main -> showMainActionBarMenu()
            R.id.toolbar_selection_delete -> presenter.delete()
            R.id.toolbar_item_open -> showSingleFragmentFilePicker()
        }
        return true
    }

    override fun onSwipeRefresh(): Boolean {
        if (!super.onSwipeRefresh()) {
            presenter.getItemsById(LocalContentTools.getDir(requireContext()))
            return true
        }
        return false
    }

    override fun onStateUpdateRoot(isRoot: Boolean) {
        activity?.apply {
            setAppBarStates(false)
            showNavigationButton(!isRoot)
            showAccount(false)
        }
    }

    override fun onStateMenuSelection() {
        if (menu != null && menuInflater != null && context != null) {
            menuInflater?.inflate(R.menu.docs_select, menu)
            deleteItem = menu?.findItem(R.id.toolbar_selection_delete)?.apply {
                UiUtils.setMenuItemTint(requireContext(), this, lib.toolkit.base.R.color.colorPrimary)
                isVisible = true
            }
            activity?.showNavigationButton(true)
        }
    }

    override fun onStateEmptyBackStack() {
        swipeRefreshLayout?.isRefreshing = true
        presenter.getItemsById(LocalContentTools.getDir(requireContext()))
    }

    override fun onStateUpdateFilter(isFilter: Boolean, value: String?) {
        super.onStateUpdateFilter(isFilter, value)
        activity?.showNavigationButton(isFilter)
    }

    override fun onListEnd() {
        // Stub to local
    }

    override fun onActionBarTitle(title: String) {
        setActionBarTitle(title)
    }

    override fun onActionButtonClick(buttons: ActionBottomDialog.Buttons?) {
        super.onActionButtonClick(buttons)
        if (buttons == ActionBottomDialog.Buttons.PHOTO) {
            if (checkCameraPermission()) {
                makePhoto()
            }
        } else if(buttons == ActionBottomDialog.Buttons.IMPORT) {
            if(checkReadPermission()) {
                importFile.launch(arrayOf(ActivitiesUtils.PICKER_NO_FILTER))
            }
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        var string = value
        tag?.let {
            string = string?.trim { it <= ' ' }
            when (tag) {
                TAG_STORAGE_IMPORT -> requestManage(REQUEST_STORAGE_IMPORT)
                TAG_STORAGE_ACCESS -> requestManage(REQUEST_STORAGE_ACCESS)
                DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_SELECTED -> presenter.deleteItems()
                DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME -> string?.let {
                    presenter.rename(it)
                }
                DocsBasePresenter.TAG_DIALOG_ACTION_SHEET -> presenter.createDocs(
                    "$string." + ApiContract.Extension.XLSX.lowercase(Locale.ROOT)
                )
                DocsBasePresenter.TAG_DIALOG_ACTION_PRESENTATION -> presenter.createDocs(
                    "$string." + ApiContract.Extension.PPTX.lowercase(Locale.ROOT)
                )
                DocsBasePresenter.TAG_DIALOG_ACTION_DOC -> presenter.createDocs(
                    "$string." + ApiContract.Extension.DOCX.lowercase(Locale.ROOT)
                )
                DocsBasePresenter.TAG_DIALOG_ACTION_FOLDER -> string?.let {
                    presenter.createFolder(it)
                }
                DocsBasePresenter.TAG_DIALOG_DELETE_CONTEXT -> presenter.deleteFile()
                else -> {
                }
            }
        }
        hideDialog()
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        if (tag == TAG_STORAGE_ACCESS) {
            preferenceTool?.isShowStorageAccess = false
            presenter.recreateStack()
            presenter.getItemsById(LocalContentTools.getDir(requireContext()))
        }
    }

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        when (buttons) {
            ContextBottomDialog.Buttons.DOWNLOAD -> presenter.upload()
            ContextBottomDialog.Buttons.DELETE -> presenter.showDeleteDialog()
            ContextBottomDialog.Buttons.COPY -> {
                operation = Operation.COPY
                showFolderChooser(BaseActivity.REQUEST_SELECT_FOLDER)
            }
            ContextBottomDialog.Buttons.MOVE -> {
                operation = Operation.MOVE
                showFolderChooser(BaseActivity.REQUEST_SELECT_FOLDER)
            }
            ContextBottomDialog.Buttons.RENAME -> showEditDialogRename(
                getString(R.string.dialogs_edit_rename_title),
                presenter.itemTitle,
                getString(R.string.dialogs_edit_hint),
                DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                getString(R.string.dialogs_edit_accept_rename),
                getString(R.string.dialogs_common_cancel_button),
                presenter.itemExtension
            )
            else -> {
            }
        }
        contextBottomDialog?.dismiss()
    }

    override fun onActionDialog() {
        actionBottomDialog?.let {
            it.onClickListener = this
            it.isLocal = true
            it.show(parentFragmentManager, ActionBottomDialog.TAG)
        }
    }

    override fun onRemoveItem(item: Item) {
        explorerAdapter?.let {
            it.removeItem(item)
            it.checkHeaders()
            setPlaceholder(it.itemList?.size == 0)
            onClearMenu()
        }
    }

    override fun onRemoveItems(items: List<Item>) {
        explorerAdapter?.let {
            it.removeItems(ArrayList<Entity>(items))
            it.checkHeaders()
            setPlaceholder(it.itemList?.size == 0)
            onClearMenu()
        }
    }

    override fun onShowFolderChooser() {
        showFolderChooser(BaseActivity.REQUEST_SELECT_FOLDER)
    }

    override fun onShowCamera(photoUri: Uri) {
        this.startActivityForResult(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }, BaseActivity.REQUEST_ACTIVITY_CAMERA
        )
    }

    override fun onShowDocs(uri: Uri, isNew: Boolean) {
        showEditors(uri, EditorsType.DOCS, isNew)
    }

    override fun onShowCells(uri: Uri) {
        showEditors(uri, EditorsType.CELLS)
    }

    override fun onShowSlides(uri: Uri) {
        showEditors(uri, EditorsType.PRESENTATION)
    }

    override fun onShowPdf(uri: Uri) {
        showEditors(uri, EditorsType.PDF)
    }

    override fun onOpenMedia(state: OpenState.Media) {
        MediaActivity.show(this, state.explorer, state.isWebDav)
    }

    override fun onShowPortals() {
        PortalsActivity.showPortals(this)
    }

    override fun setVisibilityActionButton(isShow: Boolean) {
        activity?.showActionButton(isShow)
    }

    private fun init() {
        presenter.checkBackStack()
        // Check shortcut
        val bundle = requireActivity().intent.extras
        if (bundle != null && bundle.containsKey(KEY_SHORTCUT)) {
            when (bundle.getString(KEY_SHORTCUT)) {
                LocalContentTools.DOCX_EXTENSION -> {
                    onActionButtonClick(ActionBottomDialog.Buttons.DOC)
                }
                LocalContentTools.XLSX_EXTENSION -> {
                    onActionButtonClick(ActionBottomDialog.Buttons.SHEET)
                }
                LocalContentTools.PPTX_EXTENSION -> {
                    onActionButtonClick(ActionBottomDialog.Buttons.PRESENTATION)
                }
            }
            requireActivity().intent.extras?.clear()
        }
    }

    private fun makePhoto() {
        presenter.createPhoto()
    }

    private fun showSingleFragmentFilePicker() {
        try {
            openFile.launch(arrayOf(ActivitiesUtils.PICKER_NO_FILTER))
        } catch (e: ActivityNotFoundException) {
            onError(e.message)
        }
    }

    override fun onError(message: String?) {
        if(message?.contains(getString(R.string.errors_import_local_file_desc)) == true) {
            showSnackBar(R.string.errors_import_local_file)
        } else {
            super.onError(message)
        }
    }

    private fun checkStorage(tag: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !Environment.isExternalStorageManager() &&
            preferenceTool?.isShowStorageAccess == true
        ) {

            //TODO удалить когда будет доступно разрешение
//            preferenceTool?.isShowStorageAccess = false
//            presenter.recreateStack()
//            presenter.getItemsById(LocalContentTools.getDir(requireContext()))

            //TODO раскоментировать когда будет доступно разрешение
            showQuestionDialog(
                getString(R.string.app_manage_files_title),
                getString(R.string.app_manage_files_description),
                getString(R.string.dialogs_common_ok_button),
                getString(R.string.dialogs_common_cancel_button),
                tag
            );
        }
    }

    private fun requestManage(tag: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:" + requireContext().packageName)
                )
                startActivityForResult(intent, tag)
            } catch (e: ActivityNotFoundException) {
                showSnackBar("Not found")
                placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.ACCESS)
            }
        }
    }

    private fun setPlaceholder(isEmpty: Boolean) {
        onPlaceholder(if (isEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
    }

    override val isActivePage: Boolean
        get() = isAdded

    override val isWebDav: Boolean
        get() = false

    override fun showMainActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showMainActionBarMenu(listOf(MainActionBarPopup.Author))
    }

    override fun showSelectedActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showSelectedActionBarMenu(
            excluded = listOf(
                SelectActionBarPopup.Restore,
                SelectActionBarPopup.Download
            )
        )
    }

    fun showRoot() {
        presenter.recreateStack()
        presenter.getItemsById(LocalContentTools.getDir(requireContext()))
        presenter.updateState()
        onScrollToPosition(0)
    }

    override val selectActionBarClickListener: (ActionBarPopupItem) -> Unit = {
        when (it) {
            SelectActionBarPopup.Copy -> {
                operation = Operation.COPY
                showFolderChooser(BaseActivity.REQUEST_SELECT_FOLDER)
            }
            SelectActionBarPopup.Move -> {
                operation = Operation.MOVE
                showFolderChooser(BaseActivity.REQUEST_SELECT_FOLDER)
            }
            else -> {
                super.selectActionBarClickListener(it)
            }
        }
    }

    companion object {
        val TAG: String = DocsOnDeviceFragment::class.java.simpleName

        private const val TAG_STORAGE_ACCESS = "TAG_STORAGE_ACCESS"
        private const val TAG_STORAGE_IMPORT = "TAG_STORAGE_IMPORT"

        private const val REQUEST_STORAGE_IMPORT = 10007

        private const val KEY_SHORTCUT = "create_type"

        fun newInstance(): DocsOnDeviceFragment {
            return DocsOnDeviceFragment()
        }
    }
}