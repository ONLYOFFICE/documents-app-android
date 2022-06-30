package app.editors.manager.ui.fragments.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.DiffUtil
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.views.base.BaseViewExt
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.storages.onedrive.ui.fragments.DocsOneDriveFragment
import app.editors.manager.ui.activities.main.MainActivity.Companion.show
import app.editors.manager.ui.activities.main.MediaActivity.Companion.show
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.diffutilscallback.EntityDiffUtilsCallback
import app.editors.manager.ui.adapters.holders.factory.TypeFactory
import app.editors.manager.ui.adapters.holders.factory.TypeFactoryExplorer.Companion.factory
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog.DialogButtonOnClick
import app.editors.manager.ui.fragments.base.ListFragment
import app.editors.manager.ui.popup.MainActionBarPopup
import app.editors.manager.ui.popup.SelectActionBarPopup
import app.editors.manager.ui.views.custom.CommonSearchView
import app.editors.manager.ui.views.custom.PlaceholderViews
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.ActivitiesUtils.createFile
import lib.toolkit.base.managers.utils.ActivitiesUtils.getExternalStoragePermission
import lib.toolkit.base.managers.utils.EditorsContract
import lib.toolkit.base.managers.utils.PermissionUtils.requestReadPermission
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getHelpUrl
import lib.toolkit.base.managers.utils.StringUtils.getNameWithoutExtension
import lib.toolkit.base.managers.utils.TimeUtils.fileTimeStamp
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.BaseAdapter.OnItemContextListener
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog.OnBottomDialogCloseListener
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import lib.toolkit.base.ui.dialogs.common.CommonDialog.OnCommonDialogClose
import lib.toolkit.base.ui.popup.ActionBarPopupItem

abstract class DocsBaseFragment : ListFragment(), DocsBaseView, BaseAdapter.OnItemClickListener,
    OnItemContextListener, BaseAdapter.OnItemLongClickListener, ContextBottomDialog.OnClickListener,
    ActionBottomDialog.OnClickListener, SearchView.OnQueryTextListener, DialogButtonOnClick, LifecycleObserver {

    protected enum class EditorsType {
        DOCS, CELLS, PRESENTATION, PDF
    }

    /*
     * Toolbar menu
     * */
    protected var searchItem: MenuItem? = null
    protected var openItem: MenuItem? = null
    protected var mainItem: MenuItem? = null
    protected var deleteItem: MenuItem? = null
    protected var restoreItem: MenuItem? = null
    protected var filterItem: MenuItem? = null
    protected var searchView: SearchView? = null
    protected var searchCloseButton: ImageView? = null
    protected var explorerAdapter: ExplorerAdapter? = null

    var contextBottomDialog: ContextBottomDialog? = null
    var actionBottomDialog: ActionBottomDialog? = null
    var moveCopyDialog: MoveCopyDialog? = null

    private var mLastClickTime: Long = 0
    private var selectItem: MenuItem? = null
    private val mTypeFactory: TypeFactory = factory

    protected abstract val presenter: DocsBasePresenter<out DocsBaseView>
    protected abstract val isWebDav: Boolean?

    companion object {
        private const val CLICK_TIME_INTERVAL: Long = 350
        const val REQUEST_OPEN_FILE = 10000
        const val REQUEST_DOCS = 10001
        const val REQUEST_PRESENTATION = 10002
        const val REQUEST_SHEETS = 10003
        const val REQUEST_PDF = 10004
        const val REQUEST_DOWNLOAD = 10005
        const val REQUEST_STORAGE_ACCESS = 10006

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
        setHasOptionsMenu(true)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onDialogListener() {
        contextBottomDialog?.onClickListener = this
        actionBottomDialog?.onClickListener = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isActivePage && resultCode == Activity.RESULT_CANCELED && requestCode == BaseActivity.REQUEST_ACTIVITY_OPERATION) {
            onRefresh()
        } else if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_DOCS,
                REQUEST_SHEETS,
                REQUEST_PRESENTATION -> removeCommonDialog()
                REQUEST_DOWNLOAD ->
                    data?.let {
                        activity?.let { activity ->
                            it.data?.let { uri ->
                                getExternalStoragePermission(activity, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            }
                        }
                        presenter.download(it.data!!)
                    }
                BaseActivity.REQUEST_ACTIVITY_MEDIA -> {
                }
            }
        }
    }

    override fun onContextDialogClose() {
        if (requireActivity() is OnBottomDialogCloseListener) {
            (requireActivity() as OnBottomDialogCloseListener).onBottomDialogClose()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isActivePage) {
            when (requestCode) {
                PERMISSION_WRITE_STORAGE -> {
                    if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        presenter.createDownloadFile()
                    }
                }
                PERMISSION_READ_STORAGE -> {
                    if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        showMultipleFilePickerActivity()
                    }
                }
                PERMISSION_CAMERA -> {
                    if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        showCameraActivity(fileTimeStamp)
                    }
                }
            }
        }
        if (requestCode == PERMISSION_READ_UPLOAD) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requireActivity().intent?.let {
                    it.clipData?.getItemAt(0)?.uri?.let { uri -> presenter.uploadToMy(uri) }
                    requireActivity().intent = null
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menu.clear()
        presenter.initMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_item_main -> showMainActionBarMenu(item.itemId)
            R.id.toolbar_selection_delete -> presenter.delete()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed(): Boolean {
        return presenter.getBackStack()
    }

    override fun onListEnd() {
        super.onListEnd()
        if (!presenter.isFilteringMode) {
            explorerAdapter?.isLoading(true)
            presenter.getNextList()
        }
    }

    override fun onRefresh() {
        onSwipeRefresh()
    }

    protected open fun onSwipeRefresh(): Boolean {
        return presenter.refresh()
    }

    /*
     * Views callbacks
     * */
    override fun onQueryTextSubmit(query: String): Boolean {
        presenter.filter(query, true)
        searchView?.onActionViewCollapsed()
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        val isEmpty = newText.isEmpty()
        if(isEmpty) {
            searchCloseButton?.alpha = 0.5f
        } else {
            searchCloseButton?.alpha = 1.0f
        }
        searchCloseButton?.isEnabled = !isEmpty
        presenter.filterWait(newText)
        return false
    }

    override fun onItemContextClick(view: View, position: Int) {
        val item = explorerAdapter?.getItem(position)
        if (item is Item && !isFastClick) {
            mContextDialogListener?.onContextDialogOpen()
            presenter.onContextClick(item, position, false)
        }
    }

    override fun onItemClick(view: View, position: Int) {
        if (!isFastClick || explorerAdapter?.isSelectMode == true) {
            presenter.onItemClick(explorerAdapter?.getItem(position) as Item, position)
        }
    }

    private val isFastClick: Boolean
        get() {
            val now = System.currentTimeMillis()
            return if (now - mLastClickTime < CLICK_TIME_INTERVAL) {
                true
            } else {
                mLastClickTime = now
                false
            }
        }

    override fun onItemLongClick(view: View, position: Int) {
        presenter.setSelection(true)
        presenter.onItemClick(explorerAdapter?.getItem(position) as Item, position)
    }

    override fun onNoProvider() {
        requireActivity().finish()
        show(requireContext())
    }

    override fun onOpenLocalFile(file: CloudFile) {
        val uri = Uri.parse(file.webUrl)
        when (getExtension(file.fileExst)) {
            StringUtils.Extension.DOC, StringUtils.Extension.FORM -> {
                presenter.addRecent(file)
                showEditors(uri, EditorsType.DOCS)
            }
            StringUtils.Extension.SHEET -> {
                presenter.addRecent(file)
                showEditors(uri, EditorsType.CELLS)
            }
            StringUtils.Extension.PRESENTATION -> {
                presenter.addRecent(file)
                showEditors(uri, EditorsType.PRESENTATION)
            }
            StringUtils.Extension.PDF -> {
                presenter.addRecent(file)
                showEditors(uri, EditorsType.PDF)
            }
            StringUtils.Extension.VIDEO_SUPPORT -> {
                presenter.addRecent(file)
                val videoFile = file.clone().apply {
                    webUrl = uri.path
                    id = ""
                }
                val explorer = Explorer().apply {
                    files = listOf(videoFile)
                }
                show(this, explorer, true)
            }
            StringUtils.Extension.UNKNOWN, StringUtils.Extension.EBOOK, StringUtils.Extension.ARCH,
            StringUtils.Extension.VIDEO, StringUtils.Extension.HTML -> {
                onSnackBar(getString(R.string.download_manager_complete))
            }
            else -> {
            }
        }
    }

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        when (buttons) {
            ContextBottomDialog.Buttons.MOVE -> presenter.moveContext()
            ContextBottomDialog.Buttons.COPY -> presenter.copyContext()
            ContextBottomDialog.Buttons.DOWNLOAD -> onFileDownloadPermission()
            ContextBottomDialog.Buttons.RENAME -> if (presenter.itemClicked is CloudFile) {
                showEditDialogRename(
                    getString(R.string.dialogs_edit_rename_title),
                    getNameWithoutExtension(presenter.itemTitle),
                    getString(R.string.dialogs_edit_hint),
                    DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                    getString(R.string.dialogs_edit_accept_rename),
                    getString(R.string.dialogs_common_cancel_button)
                )
            } else {
                showEditDialogRename(
                    getString(R.string.dialogs_edit_rename_title),
                    presenter.itemTitle,
                    getString(R.string.dialogs_edit_hint),
                    DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                    getString(R.string.dialogs_edit_accept_rename),
                    getString(R.string.dialogs_common_cancel_button)
                )
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

    override fun onActionButtonClick(buttons: ActionBottomDialog.Buttons?) {
        when (buttons) {
            ActionBottomDialog.Buttons.SHEET -> showEditDialogCreate(
                getString(R.string.dialogs_edit_create_sheet),
                getString(R.string.dialogs_edit_create_sheet),
                getString(R.string.dialogs_edit_hint),
                "." + ApiContract.Extension.XLSX.lowercase(),
                DocsBasePresenter.TAG_DIALOG_ACTION_SHEET,
                getString(R.string.dialogs_edit_accept_create),
                getString(R.string.dialogs_common_cancel_button)
            )
            ActionBottomDialog.Buttons.PRESENTATION -> showEditDialogCreate(
                getString(R.string.dialogs_edit_create_presentation),
                getString(R.string.dialogs_edit_create_presentation),
                getString(R.string.dialogs_edit_hint),
                "." + ApiContract.Extension.PPTX.lowercase(),
                DocsBasePresenter.TAG_DIALOG_ACTION_PRESENTATION,
                getString(R.string.dialogs_edit_accept_create),
                getString(R.string.dialogs_common_cancel_button)
            )
            ActionBottomDialog.Buttons.DOC -> showEditDialogCreate(
                getString(R.string.dialogs_edit_create_docs),
                getString(R.string.dialogs_edit_create_docs),
                getString(R.string.dialogs_edit_hint),
                "." + ApiContract.Extension.DOCX.lowercase(),
                DocsBasePresenter.TAG_DIALOG_ACTION_DOC,
                getString(R.string.dialogs_edit_accept_create),
                getString(R.string.dialogs_common_cancel_button)
            )
            ActionBottomDialog.Buttons.FOLDER -> showEditDialogCreate(
                getString(R.string.dialogs_edit_create_folder),
                "",
                getString(R.string.dialogs_edit_hint),
                null,
                DocsBasePresenter.TAG_DIALOG_ACTION_FOLDER,
                getString(R.string.dialogs_edit_accept_create),
                getString(R.string.dialogs_common_cancel_button)
            )
            ActionBottomDialog.Buttons.UPLOAD -> presenter.uploadPermission()
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (isResumed) {
            tag?.let {
                when (it) {
                    DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT -> presenter.delete()
                    DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME -> presenter.rename(value)
                    DocsBasePresenter.TAG_DIALOG_ACTION_FOLDER -> presenter.createFolder(value)
                    DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_SELECTED -> presenter.deleteItems()
                    DocsBasePresenter.TAG_DIALOG_ACTION_SHEET -> presenter.createDocs(
                        value
                                + "." + ApiContract.Extension.XLSX.lowercase()
                    )
                    DocsBasePresenter.TAG_DIALOG_ACTION_PRESENTATION -> presenter.createDocs(
                        value
                                + "." + ApiContract.Extension.PPTX.lowercase()
                    )
                    DocsBasePresenter.TAG_DIALOG_ACTION_DOC -> presenter.createDocs(
                        value
                                + "." + ApiContract.Extension.DOCX.lowercase()
                    )
                    else -> {
                    }
                }
            }
        }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        tag?.let {
            when (it) {
                DocsBasePresenter.TAG_DIALOG_CANCEL_DOWNLOAD -> presenter.cancelDownload()
                DocsBasePresenter.TAG_DIALOG_CANCEL_UPLOAD -> presenter.cancelUpload()
                DocsBasePresenter.TAG_DIALOG_CANCEL_SINGLE_OPERATIONS -> presenter.cancelSingleOperationsRequests()
                DocsBasePresenter.TAG_DIALOG_CANCEL_BATCH_OPERATIONS -> {
                    presenter.terminate()
                    return
                }
            }
        }
        super.onCancelClick(dialogs, tag)
    }

    /*
     * Presenter callbacks
     * */
    override fun onError(message: String?) {
        resetIndicators()
        hideDialog()
        message?.let {
//            TODO add webdav exception
            if (it == "HTTP 503 Service Unavailable") {
                setAccessDenied()
                presenter.clearStack()
                return
            } else if (it == getString(R.string.errors_client_host_not_found) || it == getString(
                    R.string.errors_client_unauthorized
                )
            ) {
                if (requireActivity() is BaseViewExt) {
                    (requireActivity() as BaseViewExt).onUnauthorized(it)
                    return
                }
            }
            showSnackBar(it)
        }
    }

    private fun setAccessDenied() {
        resetIndicators()
        setVisibilityActionButton(false)
        setScrollViewPager(false)
        setVisibleTabs(false)
        onStateMenuEnabled(false)
        setActionBarTitle("")
        onPlaceholder(PlaceholderViews.Type.ACCESS)
        presenter.setAccessDenied()
    }

    override fun onUnauthorized(message: String?) {
        requireActivity().finish()
        show(requireContext())
    }

    /*
     * Docs
     * */
    override fun onDocsGet(list: List<Entity>?) {
        val isEmpty = list?.isEmpty() ?: false
        setViewState(isEmpty)
        onStateMenuEnabled(!isEmpty)
        explorerAdapter?.setItems(list)
    }

    override fun onDocsRefresh(list: List<Entity>?) {
        val isEmpty = list?.isEmpty() ?: false
        setViewState(isEmpty)
        onStateMenuEnabled(!isEmpty)
        explorerAdapter?.setItems(list)
        recyclerView?.scheduleLayoutAnimation()
    }

    override fun onDocsFilter(list: List<Entity>?) {
        val isEmpty = list != null && list.isEmpty()
        setViewState(isEmpty)
        explorerAdapter?.setItems(list)
    }

    override fun onDocsNext(list: List<Entity>?) {
        setViewState(false)
        explorerAdapter?.setItems(list)
    }

    override fun onDocsAccess(isAccess: Boolean, message: String) {
        setContextDialogExternalLinkEnable(true)
        setContextDialogExternalLinkSwitch(isAccess, message)
    }

    override fun onFinishDownload(uri: Uri?) {
        activity?.let { activity ->
            uri?.let { uri ->
                ActivitiesUtils.releaseExternalStoragePermission(activity, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
    }

    override fun onDocsBatchOperation() {
        // Stub
    }

    /*
     * Views states
     * */
    override fun onStateUpdateRoot(isRoot: Boolean) {
        if (isActivePage) {
            setViewsModalState(!isRoot)
        }
    }

    override fun onStateUpdateFilter(isFilter: Boolean, value: String?) {
        if (isActivePage) {
            if (isFilter) {
                setViewsModalState(true)
                // Set previous text in search field
                if (searchView?.query.toString().isEmpty()) {
                    searchView?.setQuery(value, false)
                }

                // Set close button visibility
                searchCloseButton?.let {
                    val isEmpty = value?.isEmpty() ?: false
                    it.isEnabled = !isEmpty
                }
            } else {
                searchView?.let {
                    it.setQuery("", false)
                    if (!it.isIconified) {
                        it.isIconified = true
                    }
                }
            }
        }
    }

    override fun onStateUpdateSelection(isSelection: Boolean) {
        if (isActivePage) {
            if (isSelection) {
                setViewsModalState(true)
                setVisibilityActionButton(false)
                explorerAdapter?.isSelectMode = true
            } else {
                setVisibilityActionButton(true)
                explorerAdapter?.isSelectMode = false
            }
            menu?.let {
                onCreateOptionsMenu(it, requireActivity().menuInflater)
//                it.findItem(R.id.toolbar_selection_select_all)?.isVisible = !presenter.isSelectedAll
            }
        }
    }

    override fun onStateAdapterRoot(isRoot: Boolean) {
        explorerAdapter?.isRoot = isRoot
    }

    override fun onStateMenuDefault(sortBy: String, isAsc: Boolean) {
        if (menu != null && menuInflater != null) {
            menu?.let { menu ->
                menuInflater?.let { inflater ->
                    inflater.inflate(R.menu.docs_main, menu)
                    openItem = menu.findItem(R.id.toolbar_item_open)
                    mainItem = menu.findItem(R.id.toolbar_item_main)
                    filterItem = menu.findItem(R.id.toolbar_item_filter)
                    searchItem = menu.findItem(R.id.toolbar_item_search)
                    searchView = initSearchView(searchItem?.actionView as? SearchView)
                    presenter.initMenuSearch()
                    presenter.initMenuState()
                }
            }
        }
    }

    private fun initSearchView(searchView: SearchView?): SearchView {
        return CommonSearchView(
            searchView = searchView,
            isIconified = !presenter.isFilteringMode,
            queryTextListener = this@DocsBaseFragment,
            searchClickListener = { presenter.setFiltering(true) },
            closeClickListener = { if (!isSearchViewClear) onBackPressed() }
        ).also {
            searchCloseButton = it.closeButton
        }.build()
    }

    override fun onStateMenuSelection() {
        // Stub
    }

    override fun onStateMenuEnabled(isEnabled: Boolean) {
        setMenuMainEnabled(isEnabled)
        setMenuSearchEnabled(isEnabled)
    }

    override fun onStateActionButton(isVisible: Boolean) {
        if (isActivePage) {
            setVisibilityActionButton(isVisible)
        }
    }

    override fun onStateEmptyBackStack() {
        // Stub
    }

    /*
     * Changes
     * */
    override fun onCreateFolder(folder: CloudFolder) {
        // Stub
    }

    override fun onCreateFile(file: CloudFile) {
        showViewerActivity(file)
    }

    override fun onDeleteBatch(list: List<Entity>) {
        explorerAdapter?.let { adapter ->
            val callback = EntityDiffUtilsCallback(list, adapter.itemList)
            val result = DiffUtil.calculateDiff(callback)
            adapter.setData(list)
            result.dispatchUpdatesTo(adapter)
        }
    }

    override fun onRename(item: Item, position: Int) {
        explorerAdapter?.setItem(item, position)
    }

    override fun onBatchMove(explorer: Explorer) {
        if (presenter.isTrashMode) {
            showOperationRestoreActivity(explorer)
        } else {
            showOperationMoveActivity(explorer)
        }
    }

    override fun onBatchCopy(explorer: Explorer) {
        showOperationCopyActivity(explorer)
    }

    override fun onActionBarTitle(title: String) {
        // Stub
    }

    override fun onItemsSelection(countSelected: String) {
        onActionBarTitle(countSelected)
        explorerAdapter?.notifyDataSetChanged()
    }

    override fun onItemSelected(position: Int, countSelected: String) {
        onActionBarTitle(countSelected)
        explorerAdapter?.notifyItemChanged(position)
    }

    override fun onItemContext(state: ContextBottomDialog.State) {
        showContextDialog(state)
    }

    override fun onActionDialog(isThirdParty: Boolean, isDocs: Boolean) {
        actionBottomDialog?.let { dialog ->
            dialog.onClickListener = this
            dialog.isThirdParty = isThirdParty
            dialog.isDocs = isDocs
            dialog.show(parentFragmentManager, ActionBottomDialog.TAG)
        }
    }

    override fun onDownloadActivity(uri: Uri) {
        showDownloadFolderActivity(uri)
    }

    override fun onFileMedia(explorer: Explorer, isWebDAv: Boolean) {
        showMediaActivity(explorer, isWebDAv)
    }

    override fun onFileDownloadPermission() {
        presenter.createDownloadFile()
    }

    override fun onFileUploadPermission() {
        if (checkReadPermission()) {
            showMultipleFilePickerActivity()
        }
    }

    override fun onCreateDownloadFile(name: String) {
        createFile(this, name, REQUEST_DOWNLOAD)
    }

    override fun onScrollToPosition(position: Int) {
        recyclerView?.scrollToPosition(position)
    }

    override fun onSwipeEnable(isSwipeEnable: Boolean) {
        swipeRefreshLayout?.isRefreshing = isSwipeEnable
    }

    override fun onPlaceholder(type: PlaceholderViews.Type) {
        placeholderViews?.setTemplatePlaceholder(type)
    }

    override fun onDialogClose() {
        if (isActivePage) {
            hideDialog()
        }
    }

    override fun onDialogWaiting(title: String?, tag: String?) {
        if (isActivePage) {
            showWaitingDialog(title, getString(R.string.dialogs_common_cancel_button), tag)
        }
    }

    override fun onDialogQuestion(title: String?, question: String?, tag: String?) {
        if (isActivePage) {
            showQuestionDialog(
                title!!, question, getString(R.string.dialogs_question_accept_yes),
                getString(R.string.dialogs_question_accept_no), tag!!
            )
        }
    }

    override fun onDialogProgress(title: String?, isHideButtons: Boolean, tag: String?) {
        if (isActivePage) {
            showProgressDialog(
                title,
                isHideButtons,
                getString(R.string.dialogs_common_cancel_button),
                tag
            )
        }
    }

    override fun onDialogProgress(total: Int, progress: Int) {
        if (isActivePage) {
            updateProgressDialog(total, progress)
        }
    }

    override fun onSnackBar(message: String) {
        if (isActivePage) {
            showSnackBar(message)
        }
    }

    override fun onSnackBarWithAction(message: String, button: String, action: View.OnClickListener) {
        if (isActivePage) {
            showSnackBarWithAction(message, button, action)
        }
    }

    override fun onClearMenu() {
        menu?.let {
            if (explorerAdapter?.itemList?.size == 0) {
                it.findItem(R.id.toolbar_item_empty_trash).isVisible = false
                searchItem?.isVisible = false
            }
        }
    }

    override fun onUploadFileProgress(progress: Int, id: String) {
        val uploadFile = explorerAdapter?.getUploadFileById(id)
        uploadFile?.let { file ->
            file.progress = progress
            explorerAdapter?.updateItem(file)
        }
    }

    override fun onDeleteUploadFile(id: String) {
        explorerAdapter?.removeUploadItemById(id)
    }

    override fun onRemoveUploadHead() {
        explorerAdapter?.removeHeader(getApp().getString(R.string.upload_manager_progress_title))
    }

    override fun onAddUploadsFile(uploadFiles: List<Entity>) {
        onRemoveUploadHead()
        explorerAdapter?.addItemsAtTop(uploadFiles)
        explorerAdapter?.addItemAtTop(Header(getString(R.string.upload_manager_progress_title)))
        recyclerView?.scrollToPosition(0)
    }

    override fun continueClick(tag: String?, action: String?) {
        var operationType = ApiContract.Operation.OVERWRITE
        tag?.let {
            when (it) {
                MoveCopyDialog.TAG_DUPLICATE -> operationType = ApiContract.Operation.DUPLICATE
                MoveCopyDialog.TAG_OVERWRITE -> operationType = ApiContract.Operation.OVERWRITE
                MoveCopyDialog.TAG_SKIP -> operationType = ApiContract.Operation.SKIP
            }
            if (action == MoveCopyDialog.ACTION_COPY) {
                presenter.transfer(operationType, false)
            } else {
                presenter.transfer(operationType, true)
            }
        }
    }

    override fun onActionDialogClose() {
        if (requireActivity() is OnBottomDialogCloseListener) {
            (requireActivity() as OnBottomDialogCloseListener?)!!.onBottomDialogClose()
        }
    }

    override fun onCloseCommonDialog() {
        if (requireActivity() is OnCommonDialogClose) {
            (requireActivity() as OnCommonDialogClose?)!!.onCommonClose()
        }
    }

    /*
     * Clear SearchView
     * */
    private val isSearchViewClear: Boolean
        private get() {
            if (searchView?.query?.isNotEmpty() == true) {
                searchView?.setQuery("", true)
                return true
            }
            return false
        }

    /*
     * On pager scroll callback
     * */
    open fun onScrollPage() {
        presenter.initViews()
    }

    /*
     * Menu methods
     * */
    protected open fun setMenuMainEnabled(isEnabled: Boolean) {
        mainItem?.isVisible = isEnabled
    }

    fun setMenuSearchEnabled(isEnabled: Boolean) {
        searchItem?.isVisible = isEnabled
    }

    /*
     * Initialisations
     * */
    private fun init() {
        setDialogs()
        explorerAdapter = ExplorerAdapter(mTypeFactory).apply {
            setOnItemContextListener(this@DocsBaseFragment)
            setOnItemClickListener(this@DocsBaseFragment)
            setOnItemLongClickListener(this@DocsBaseFragment)
        }

        recyclerView?.adapter = explorerAdapter
        recyclerView?.setPadding(
            resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_left_right_padding),
            resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_top_bottom_padding),
            resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_left_right_padding),
            resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_bottom_padding)
        )
    }

    /*
     * Views states for root/empty and etc...
     * */
    private fun setViewsModalState(isModal: Boolean) {
        if (isActivePage) {
            setToolbarState(!isModal)
            setScrollViewPager(!isModal)
        }
    }

    private fun expandRootViews() {
        if (isActivePage) {
            setExpandToolbar()
        }
    }

    private fun resetIndicators() {
        swipeRefreshLayout?.let { swipeRefresh ->
            swipeRefresh.post {
                swipeRefresh.isRefreshing = false
            }
        }
        explorerAdapter?.isLoading(false)
        setContextDialogExternalLinkEnable(true)
    }

    private fun setViewState(isEmpty: Boolean) {
        resetIndicators()
        if (isEmpty) {
            expandRootViews()
        }
    }

    fun showFolderChooser(requestCode: Int) {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        startActivityForResult(i, requestCode)
    }

    /*
     * BottomSheetFragmentDialogs context/action
     * */
    private fun setDialogs() {
        contextBottomDialog = parentFragmentManager.findFragmentByTag(ContextBottomDialog.TAG)?.let {
            it as ContextBottomDialog?
        } ?: ContextBottomDialog.newInstance()

        actionBottomDialog = parentFragmentManager.findFragmentByTag(ActionBottomDialog.TAG)?.let {
            it as ActionBottomDialog?
        } ?: ActionBottomDialog.newInstance()

        moveCopyDialog = parentFragmentManager.findFragmentByTag(MoveCopyDialog.TAG) as MoveCopyDialog?
        moveCopyDialog?.dialogButtonOnClick = this
    }

    private fun showContextDialog(state: ContextBottomDialog.State) {
        contextBottomDialog?.let { dialog ->
            dialog.state = state
            dialog.onClickListener = this
            dialog.show(parentFragmentManager, ContextBottomDialog.TAG)
        }
    }

    private fun setContextDialogExternalLinkSwitch(isCheck: Boolean, message: String) {
        contextBottomDialog?.let { dialog ->
            dialog.setItemSharedState(isCheck)
            dialog.showMessage(message, requireView())
        }
    }

    fun setContextDialogExternalLinkEnable(isEnable: Boolean) {
        contextBottomDialog?.setItemSharedEnable(isEnable)
    }

    fun showActionDialog() {
        if (!isFastClick) {
            presenter.onActionClick()
        }
    }

    protected val args: Unit
        get() {
            requireActivity().intent?.let { intent ->
                intent.clipData?.let {
                    startUpload(intent, intent.action)
                }
            }
        }

    fun getArgs(intent: Intent?) {
        intent?.clipData?.let {
            startUpload(intent, intent.action)
        }
    }

    private fun startUpload(intent: Intent, action: String?) {
        intent.clipData?.let { data ->
            val uri = data.getItemAt(0).uri
            if (action != null && action == Intent.ACTION_SEND && uri != null) {
                if (requestReadPermission(this, PERMISSION_READ_UPLOAD)) {
                    presenter.uploadToMy(uri)
                    requireActivity().intent = null
                }
            }
        }
    }

    /*
     * Parent ViewPager methods. Check instanceof for trash fragment
     * */
    private fun setScrollViewPager(isScroll: Boolean) {
        val fragment = parentFragment
        if (fragment is MainPagerFragment) {
            fragment.setScrollViewPager(isScroll)
        }
    }

    private fun setVisibleTabs(isVisible: Boolean) {
        val fragment = parentFragment
        if (fragment is MainPagerFragment) {
            fragment.setVisibleTabs(isVisible)
        }
    }

    open fun setToolbarState(isVisible: Boolean) {
        val fragment = parentFragment
        if (fragment is MainPagerFragment) {
            fragment.setToolbarState(isVisible)
        } else if (fragment is DocsOneDriveFragment) {
            fragment.setToolbarState(isVisible)
        }
    }

    open fun setExpandToolbar() {
        val fragment = parentFragment
        if (fragment is MainPagerFragment) {
            fragment.setExpandToolbar()
        }
    }

    open fun setVisibilityActionButton(isShow: Boolean) {
        val fragment = parentFragment
        if (fragment is MainPagerFragment) {
            fragment.setVisibilityActionButton(isShow)
        }
    }

    open val isActivePage: Boolean
        get() {
            return when (val fragment = parentFragment) {
                is MainPagerFragment -> fragment.isActivePage(this)
                null -> true
                else -> true
            }
        }

    fun setAccountEnable(isEnable: Boolean) {
        val fragment = parentFragment
        if (fragment is MainPagerFragment) {
            fragment.setAccountEnable(isEnable)
        }
    }

    protected fun showEditors(uri: Uri?, type: EditorsType) {
        try {
            val intent = Intent().apply {
                data = uri
                putExtra(EditorsContract.KEY_HELP_URL, getHelpUrl(requireContext()))
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            when (type) {
                EditorsType.DOCS -> {
                    intent.setClassName(requireContext(), EditorsContract.EDITOR_DOCUMENTS)
                    startActivityForResult(intent, REQUEST_DOCS)
                }
                EditorsType.CELLS -> {
                    intent.setClassName(requireContext(), EditorsContract.EDITOR_CELLS)
                    startActivityForResult(intent, REQUEST_SHEETS)
                }
                EditorsType.PRESENTATION -> {
                    intent.setClassName(requireContext(), EditorsContract.EDITOR_SLIDES)
                    startActivityForResult(intent, REQUEST_PRESENTATION)
                }
                EditorsType.PDF -> {
                    intent.setClassName(requireContext(), EditorsContract.PDF)
                    startActivity(intent)
                }
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            showToast("Not found")
        }
    }

    private fun removeCommonDialog() {
        val fragment = parentFragmentManager.findFragmentByTag(CommonDialog.TAG)
        fragment?.let {
            parentFragmentManager
                .beginTransaction()
                .remove(it)
                .commit()
        }
    }

    protected open fun showMainActionBarMenu(
        itemId: Int,
        excluded: List<ActionBarPopupItem> = emptyList()
    ) {
        val view = requireActivity().findViewById<View>(itemId)
        if (!presenter.isSelectionMode) {
            MainActionBarPopup(
                context = requireContext(),
                clickListener = mainActionBarClickListener,
                sortBy = presenter.preferenceTool.sortBy.orEmpty(),
                isAsc = isAsc,
                excluded = excluded
            ).show(view)
        } else {
            showSelectedActionBarMenu(view)
        }
    }

    protected open fun showSelectedActionBarMenu(
        view: View,
        excluded: List<ActionBarPopupItem> = emptyList()
    ) {
        SelectActionBarPopup(
            context = requireContext(),
            clickListener = selectActionBarClickListener,
            excluded = excluded
        ).show(view)
    }

    private val isAsc: Boolean
        get() = presenter.preferenceTool.sortOrder.equals(
            ApiContract.Parameters.VAL_SORT_ORDER_ASC,
            ignoreCase = true
        )

    protected open val mainActionBarClickListener: (ActionBarPopupItem) -> Unit = { item ->
        val isRepeatedTap = item == MainActionBarPopup.getSortPopupItem(presenter.preferenceTool.sortBy)
        when (item) {
            MainActionBarPopup.Select -> presenter.setSelection(true)
            MainActionBarPopup.SelectAll -> presenter.setSelectionAll()
            MainActionBarPopup.Date ->
                presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_UPDATED, isRepeatedTap)
            MainActionBarPopup.Type ->
                presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_TYPE, isRepeatedTap)
            MainActionBarPopup.Size ->
                presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_SIZE, isRepeatedTap)
            MainActionBarPopup.Title ->
                presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_TITLE, isRepeatedTap)
            MainActionBarPopup.Author ->
                presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_OWNER, isRepeatedTap)
        }
    }

    protected open val selectActionBarClickListener: (ActionBarPopupItem) -> Unit = { item ->
        when (item) {
            SelectActionBarPopup.Move -> presenter.moveSelected()
            SelectActionBarPopup.Restore -> presenter.moveSelected()
            SelectActionBarPopup.Copy -> presenter.copySelected()
            SelectActionBarPopup.Deselect -> presenter.deselectAll()
            SelectActionBarPopup.SelectAll -> presenter.selectAll()
            SelectActionBarPopup.Download -> presenter.createDownloadFile()
        }
    }
}