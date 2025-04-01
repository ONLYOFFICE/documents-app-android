package app.editors.manager.ui.fragments.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.Security
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.tools.ActionMenuAdapter
import app.editors.manager.managers.tools.ActionMenuItem
import app.editors.manager.managers.tools.ActionMenuItemsFactory
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.mvp.views.base.BaseViewExt
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity.Companion.show
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.diffutilscallback.EntityDiffUtilsCallback
import app.editors.manager.ui.adapters.holders.factory.TypeFactoryExplorer
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog.DialogButtonOnClick
import app.editors.manager.ui.dialogs.explorer.ExplorerContextBottomDialog
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.dialogs.explorer.ExplorerContextState
import app.editors.manager.ui.dialogs.fragments.OperationDialogFragment
import app.editors.manager.ui.fragments.base.ListFragment
import app.editors.manager.ui.fragments.storages.DocsOneDriveFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.CameraPicker
import lib.toolkit.base.managers.utils.CreateDocument
import lib.toolkit.base.managers.utils.EditType
import lib.toolkit.base.managers.utils.EditorsContract
import lib.toolkit.base.managers.utils.EditorsType
import lib.toolkit.base.managers.utils.FileUtils.toByteArray
import lib.toolkit.base.managers.utils.PermissionUtils.requestReadPermission
import lib.toolkit.base.managers.utils.RequestPermissions
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getHelpUrl
import lib.toolkit.base.managers.utils.TimeUtils.fileTimeStamp
import lib.toolkit.base.managers.utils.contains
import lib.toolkit.base.managers.utils.getSendFileIntent
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.BaseAdapter.OnItemContextListener
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog.OnBottomDialogCloseListener
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import lib.toolkit.base.ui.dialogs.common.CommonDialog.OnCommonDialogClose
import lib.toolkit.base.ui.dialogs.common.holders.WaitingHolder
import lib.toolkit.base.ui.popup.ActionBarMenu
import lib.toolkit.base.ui.views.search.CommonSearchView
import java.io.File

abstract class DocsBaseFragment : ListFragment(), DocsBaseView, BaseAdapter.OnItemClickListener,
    OnItemContextListener, BaseAdapter.OnItemLongClickListener, ExplorerContextBottomDialog.OnClickListener,
    ActionBottomDialog.OnClickListener, DialogButtonOnClick, LifecycleObserver {


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

    /*
     * Toolbar menu
     * */
    protected var searchItem: MenuItem? = null
    protected var openItem: MenuItem? = null
    protected var mainItem: MenuItem? = null
    protected var deleteItem: MenuItem? = null
    protected var restoreItem: MenuItem? = null
    protected var filterItem: MenuItem? = null
    protected var explorerAdapter: ExplorerAdapter? = null

    protected var searchView: CommonSearchView? = null

    var actionBottomDialog: ActionBottomDialog? = null
    var moveCopyDialog: MoveCopyDialog? = null

    private var lastClickTime: Long = 0
    private var selectItem: MenuItem? = null

    protected abstract val presenter: DocsBasePresenter<out DocsBaseView>

    private val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            actionBottomDialog?.onClickListener = this
        }
    }

    private val sendActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        presenter.removeSendingFile()
    }

    private val downloadActivityResult = registerForActivityResult(CreateDocument()) { uri ->
        uri?.let { presenter.download(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(lifecycleEventObserver)
        isGridView = presenter.preferenceTool.isGridView
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionBottomDialog = null
        moveCopyDialog = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_DOCS,
                REQUEST_SHEETS,
                REQUEST_PRESENTATION,
                -> removeCommonDialog()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isActivePage) {
            when (requestCode) {
                PERMISSION_WRITE_STORAGE -> {
                    if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        presenter.createDownloadFile()
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

    private var isFirstResume = true

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
        } else {
            view?.post {
                presenter.updateViewsState()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        presenter.initMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_item_main -> showActionBarMenu()
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

    override fun onItemContextClick(position: Int, icon: Bitmap?) {
        val item = explorerAdapter?.getItem(position) as? Item
        if (item != null && !isFastClick) {
            val roomSecurity = presenter.roomClicked?.security
            val state = ExplorerContextState(
                headerIcon = icon?.toByteArray(),
                item = item,
                headerInfo = app.editors.manager.managers.utils.StringUtils.getCloudItemInfo(
                    context = requireContext(),
                    item = item,
                    state = explorerAdapter
                ),
                sectionType = if (presenter.isRecentViaLinkSection()) {
                    ApiContract.SectionType.CLOUD_RECENT
                } else {
                    getSection().type
                },
                provider = context?.accountOnline?.portal?.provider ?: PortalProvider.default,
                isSearching = presenter.isFilteringMode,
                editIndex = presenter.isIndexing && roomSecurity?.editRoom == true,
                isRoot = presenter.isRoot
            )
            presenter.onClickEvent(item, position, true)
            showExplorerContextBottomDialog(state)
        }
    }

    override fun onItemClick(view: View, position: Int) {
        val item = explorerAdapter?.getItem(position) as Item

        if (item is CloudFile && presenter.pickerMode == PickerMode.Folders) {
            return
        }

        if (!isFastClick || explorerAdapter?.isSelectMode == true) {
            presenter.onItemClick(item, position)
        }
    }

    override fun onSendCopy(file: File) {
        sendActivityResult.launch(activity?.getSendFileIntent(lib.toolkit.base.R.string.export_send_copy, file))
    }

    private val isFastClick: Boolean
        get() {
            val now = System.currentTimeMillis()
            return if (now - lastClickTime < CLICK_TIME_INTERVAL) {
                true
            } else {
                lastClickTime = now
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

    override fun onOpenLocalFile(file: CloudFile, editType: EditType?) {
        val uri = Uri.parse(file.webUrl)
        when (getExtension(file.fileExst)) {
            StringUtils.Extension.DOC, StringUtils.Extension.FORM -> {
                presenter.addRecent(file)
                showEditors(uri, EditorsType.DOCS, editType = editType)
            }
            StringUtils.Extension.SHEET -> {
                presenter.addRecent(file)
                showEditors(uri, EditorsType.CELLS, editType = editType)
            }
            StringUtils.Extension.PRESENTATION -> {
                presenter.addRecent(file)
                showEditors(uri, EditorsType.PRESENTATION, editType = editType)
            }
            StringUtils.Extension.PDF -> {
                presenter.addRecent(file)
                showEditors(uri, EditorsType.PDF, editType = editType)
            }
            StringUtils.Extension.VIDEO_SUPPORT -> {
                presenter.addRecent(file)
                val videoFile = file.clone().apply {
                    webUrl = uri?.path.orEmpty()
                    id = ""
                }
                val explorer = Explorer().apply {
                    files = mutableListOf(videoFile)
                }
                showMediaActivity(explorer, true)
            }
            StringUtils.Extension.UNKNOWN, StringUtils.Extension.EBOOK, StringUtils.Extension.ARCH,
            StringUtils.Extension.VIDEO, StringUtils.Extension.HTML,
            -> {
                onSnackBar(getString(R.string.download_manager_complete))
            }
            else -> {
            }
        }
    }

    override fun onContextButtonClick(contextItem: ExplorerContextItem) {
        when (contextItem) {
            ExplorerContextItem.Move -> presenter.moveCopyOperation(OperationsState.OperationType.MOVE)
            ExplorerContextItem.Copy -> presenter.moveCopyOperation(OperationsState.OperationType.COPY)
            ExplorerContextItem.Send -> presenter.sendCopy()
            ExplorerContextItem.Download -> onFileDownloadPermission()
            ExplorerContextItem.Rename -> showEditDialogRename(
                title = getString(R.string.dialogs_edit_rename_title),
                value = presenter.itemTitle,
                hint = getString(R.string.dialogs_edit_hint),
                tag = DocsBasePresenter.TAG_DIALOG_CONTEXT_RENAME,
                acceptButton = getString(R.string.dialogs_edit_accept_rename),
                cancelButton = getString(R.string.dialogs_common_cancel_button),
                suffix = presenter.itemExtension
            )
            is ExplorerContextItem.Edit -> presenter.getFileInfo()
            is ExplorerContextItem.Fill -> presenter.getFileInfo()
            is ExplorerContextItem.Delete -> {
                if (presenter.isRecentViaLinkSection()) {
                    presenter.deleteItems()
                } else {
                    showDeleteDialog(tag = DocsBasePresenter.TAG_DIALOG_BATCH_DELETE_CONTEXT)
                }
            }
            else -> {}
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
                getString(R.string.dialogs_edit_create_folder),
                getString(R.string.dialogs_edit_hint),
                null,
                DocsBasePresenter.TAG_DIALOG_ACTION_FOLDER,
                getString(R.string.dialogs_edit_accept_create),
                getString(R.string.dialogs_common_cancel_button)
            )
            ActionBottomDialog.Buttons.UPLOAD -> presenter.uploadPermission()
            ActionBottomDialog.Buttons.PHOTO -> {
                presenter.createPhoto()
            }
            else -> {}
        }
    }

    override fun onShowCamera(photoUri: Uri) {
        RequestPermissions(requireActivity().activityResultRegistry, { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                CameraPicker(requireActivity().activityResultRegistry, { isCreate ->
                    if (isCreate) {
                        if (this is DocsOnDeviceFragment) {
                            onRefresh()
                        } else {
                            presenter.upload(photoUri, null)
                        }
                    } else {
                        presenter.deletePhoto()
                    }
                }, photoUri).show()
            } else {
                presenter.deletePhoto()
            }
        }, arrayOf(Manifest.permission.CAMERA)).request()
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
                    DocsBasePresenter.TAG_DIALOG_MOVE_TO_PUBLIC -> presenter.move()
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
                DocsBasePresenter.TAG_DIALOG_CLEAR_DISPOSABLE -> presenter.clearDisposable()
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
                searchView?.query = value
            } else {
                searchView?.collapse()
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
                    searchView = initSearchView(checkNotNull(searchItem?.actionView as? SearchView))
                    presenter.initMenuSearch()
                    presenter.initMenuState()
                }
            }
        }
    }

    private fun initSearchView(actionView: SearchView): CommonSearchView {
        return CommonSearchView(
            coroutineScope = lifecycleScope,
            searchView = actionView,
            isExpanded = presenter.isFilteringMode,
            onQuery = presenter::filter,
            onExpand = { presenter.setFiltering(true) },
        )
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

    /*
     * Changes
     * */

    override fun onCreateFile(file: CloudFile) {
        if (requireActivity() is IMainActivity) {
            (requireActivity() as IMainActivity).showWebViewer(file)
        }
    }

    override fun onDeleteBatch(list: List<Entity>) {
        explorerAdapter?.let { adapter ->
            val callback = EntityDiffUtilsCallback(list, adapter.itemList)
            val result = DiffUtil.calculateDiff(callback)
            adapter.setData(list)
            result.dispatchUpdatesTo(adapter)
        }
    }

    override fun onDeleteMessage(count: Int) {
        onSnackBar(resources.getQuantityString(R.plurals.operation_moved_to_trash, count))
    }

    override fun onRename(item: Item, position: Int) {
        explorerAdapter?.setItem(item, position)
    }

    override fun onBatchMoveCopy(operation: OperationsState.OperationType, explorer: Explorer) {
        OperationDialogFragment.show(
            activity = requireActivity(),
            operation = operation,
            explorer = explorer
        ) { bundle ->
            if (OperationDialogFragment.KEY_OPERATION_RESULT_COMPLETE in bundle) {
                showSnackBar(R.string.operation_complete_message)
                view?.postDelayed(::onRefresh, 500)
            }
        }
    }

    override fun onPickCloudFile(destFolderId: String) {
        OperationDialogFragment.show(
            activity = requireActivity(),
            destFolderId = destFolderId,
            explorer = Explorer()
        ) { bundle ->
            if (OperationDialogFragment.KEY_OPERATION_RESULT_COMPLETE in bundle) {
                showSnackBar(R.string.operation_complete_message)
                view?.postDelayed(::onRefresh, 500)
            }
        }
    }

    override fun onActionBarTitle(title: String) {
        // Stub
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemsSelection(countSelected: String) {
        onActionBarTitle(countSelected)
        explorerAdapter?.notifyDataSetChanged()
    }

    override fun onItemSelected(position: Int, countSelected: String) {
        onActionBarTitle(countSelected)
        explorerAdapter?.notifyItemChanged(position)
    }

    override fun onActionDialog(isThirdParty: Boolean, isDocs: Boolean, roomType: Int?) {
        actionBottomDialog?.let { dialog ->
            dialog.roomType = roomType
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
        hideDialog()
        showMediaActivity(explorer, isWebDAv)
    }

    override fun onFileDownloadPermission() {
        presenter.createDownloadFile()
    }

    override fun onFileUploadPermission(extension: String?) {
        showMultipleFilePickerActivity(extension) { uris ->
            if (!uris.isNullOrEmpty()) {
                presenter.upload(null, uris)
            }
        }
    }

    override fun onCreateDownloadFile(name: String) {
        downloadActivityResult.launch(name)
    }

    override fun onScrollToPosition(position: Int) {
        recyclerView?.scrollToPosition(position)
    }

    override fun onSwipeEnable(isSwipeEnable: Boolean) {
        swipeRefreshLayout?.isRefreshing = false
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

    override fun onDialogDownloadWaiting() {
        if (isActivePage) {
            showWaitingDialog(
                title = getString(R.string.download_manager_downloading),
                tag = null,
                type = WaitingHolder.ProgressType.CIRCLE,
                cancelButton = null,
                gravity = Gravity.CENTER,
                color = 0
            )
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

    override fun onDialogWarning(title: String, message: String, tag: String?) {
        if (isActivePage) {
            view?.post {
                getInfoDialog(
                    title = title,
                    info = message,
                    cancelTitle = getString(lib.toolkit.base.R.string.common_ok)
                )?.show(requireActivity().supportFragmentManager)
            }
        }
    }

    override fun onDialogWarning(message: String) {
        onDialogWarning(
            title = getString(R.string.dialogs_warning_title),
            message = message,
            tag = null
        )
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

    override fun onDialogDelete(count: Int, toTrash: Boolean, tag: String) {
        showDeleteDialog(count, toTrash, tag)
    }

    protected open fun showDeleteDialog(count: Int = 1, toTrash: Boolean = true, tag: String) {
        showQuestionDialog(
            title = if (count > 0) {
                resources.getQuantityString(R.plurals.dialogs_question_delete_title, count, count)
            } else {
                getString(R.string.dialogs_question_delete_all_title)
            },
            string = if (toTrash) {
                resources.getQuantityString(R.plurals.dialogs_question_message_to_trash, count)
            } else {
                resources.getQuantityString(R.plurals.dialogs_question_message_delete, count)
            },
            acceptButton = getString(R.string.dialogs_question_accept_delete),
            cancelButton = getString(R.string.dialogs_common_cancel_button),
            tag = tag,
            acceptErrorTint = true
        )
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
        if (explorerAdapter?.itemList?.size == 0) {
            searchItem?.isVisible = false
        }
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
        (requireActivity() as? OnBottomDialogCloseListener)?.onBottomDialogClose()
    }

    override fun onCloseCommonDialog() {
        (requireActivity() as? OnCommonDialogClose)?.onCommonClose()
        presenter.interruptFileSending()
    }

    override fun onSetGridView(isGrid: Boolean) {
        explorerAdapter?.isGridView = isGrid
        switchGridView(isGrid)
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
        mainItem?.isVisible = isEnabled || ApiContract.SectionType.isRoom(presenter.getSectionType())
    }

    fun setMenuSearchEnabled(isEnabled: Boolean) {
        searchItem?.isVisible = isEnabled
    }

    /*
     * Initialisations
     * */
    private fun init() {
        setDialogs()
        explorerAdapter = ExplorerAdapter(TypeFactoryExplorer.factory, presenter.preferenceTool.isGridView).apply {
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
    }

    private fun setViewState(isEmpty: Boolean) {
        resetIndicators()
        if (isEmpty) {
            expandRootViews()
        }
    }

    /*
     * BottomSheetFragmentDialogs context/action
     * */
    private fun setDialogs() {
        actionBottomDialog = parentFragmentManager.findFragmentByTag(ActionBottomDialog.TAG)?.let {
            it as ActionBottomDialog?
        } ?: ActionBottomDialog.newInstance()

        moveCopyDialog = parentFragmentManager.findFragmentByTag(MoveCopyDialog.TAG) as MoveCopyDialog?
        moveCopyDialog?.dialogButtonOnClick = this
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

    override fun onUpdateItemState() {
        fragmentListBinding?.listSwipeRefresh?.isRefreshing = false
        explorerAdapter?.updateItem(presenter.itemClicked)
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

    override fun onOpenDocumentServer(file: CloudFile?, info: String?, editType: EditType?) {
        when (getExtension(file?.fileExst ?: "")) {
            StringUtils.Extension.DOC, StringUtils.Extension.FORM -> {
                showEditors(null, EditorsType.DOCS, info, editType)
            }

            StringUtils.Extension.SHEET -> {
                showEditors(null, EditorsType.CELLS, info, editType)
            }

            StringUtils.Extension.PRESENTATION -> {
                showEditors(null, EditorsType.PRESENTATION, info, editType)
            }

            StringUtils.Extension.PDF -> {
                showEditors(null, EditorsType.PDF, info, editType)
            }

            else -> {
            }
        }
    }

    protected open fun showEditors(uri: Uri?, type: EditorsType, info: String? = null, editType: EditType? = null) {
        try {
            val intent = Intent().apply {
                data = uri
                info?.let { putExtra(EditorsContract.KEY_DOC_SERVER, info) }
                putExtra(EditorsContract.KEY_HELP_URL, getHelpUrl(requireContext()))
                putExtra(EditorsContract.KEY_EDIT_TYPE, editType)
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            when (type) {
                EditorsType.DOCS, EditorsType.PDF -> {
                    intent.setClassName(requireContext(), EditorsContract.EDITOR_DOCUMENTS)
                    if (type == EditorsType.PDF) {
                        intent.putExtra(EditorsContract.KEY_PDF, true)
                    }
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
                //                EditorsType.PDF -> {
                //                    intent.setClassName(requireContext(), EditorsContract.PDF)
                //                    startActivity(intent)
                //                }
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            showToast("Not found")
        }
    }

    protected fun getEditorsIntent(uri: Uri?, type: EditorsType, isForm: Boolean = false): Intent {
        val intent = Intent().apply {
            data = uri
            putExtra(EditorsContract.KEY_HELP_URL, getHelpUrl(requireContext()))
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        when (type) {
            EditorsType.DOCS -> {
                intent.setClassName(requireContext(), EditorsContract.EDITOR_DOCUMENTS)
            }
            EditorsType.CELLS -> {
                intent.setClassName(requireContext(), EditorsContract.EDITOR_CELLS)
            }
            EditorsType.PRESENTATION -> {
                intent.setClassName(requireContext(), EditorsContract.EDITOR_SLIDES)
            }
            EditorsType.PDF -> {
                if (isForm) {
                    intent.setClassName(requireContext(), EditorsContract.EDITOR_DOCUMENTS)
                    intent.extras?.putBoolean("pdf", true)
                } else {
                    intent.setClassName(requireContext(), EditorsContract.PDF)
                }
            }
        }
        return intent
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

    protected open fun showActionBarMenu() {
        val section = if (presenter.isRecentViaLinkSection()) ApiContract.Section.Recent else getSection()

        ActionBarMenu(
            context = requireContext(),
            adapter = ActionMenuAdapter(actionMenuClickListener),
            items = if (section.isRoom) {
                ActionMenuItemsFactory.getRoomItems(
                    section = section,
                    provider = context?.accountOnline?.portal?.provider,
                    root = presenter.isRoot,
                    selected = presenter.isSelectionMode,
                    allSelected = presenter.isSelectedAll,
                    sortBy = presenter.preferenceTool.sortBy,
                    empty = presenter.isListEmpty(),
                    currentRoom = presenter.isRoomFolder(),
                    security = presenter.roomClicked?.security ?: Security(),
                    isGridView = presenter.preferenceTool.isGridView,
                    asc = presenter.preferenceTool.sortOrder.equals(
                        ApiContract.Parameters.VAL_SORT_ORDER_ASC,
                        ignoreCase = true
                    ),
                    isIndexing = presenter.roomClicked?.indexing == true
                )
            } else {
                ActionMenuItemsFactory.getDocsItems(
                    section = section,
                    provider = context?.accountOnline?.portal?.provider,
                    selected = presenter.isSelectionMode,
                    allSelected = presenter.isSelectedAll,
                    sortBy = presenter.preferenceTool.sortBy,
                    isGridView = presenter.preferenceTool.isGridView,
                    asc = presenter.preferenceTool.sortOrder.equals(
                        ApiContract.Parameters.VAL_SORT_ORDER_ASC,
                        ignoreCase = true
                    )
                )
            }
        ).show(requireActivity().window.decorView)
    }

    protected open fun getSection(): ApiContract.Section = ApiContract.Section.getSection(presenter.getSectionType())

    protected open val actionMenuClickListener: (ActionMenuItem) -> Unit = { item ->
        when (item) {
            is ActionMenuItem.Sort -> presenter.sortBy(item.sortValue)
            is ActionMenuItem.Operation -> presenter.moveCopySelected(item.value)
            is ActionMenuItem.GridView -> presenter.setGridView(true)
            is ActionMenuItem.ListView -> presenter.setGridView(false)
            ActionMenuItem.Select -> presenter.setSelection(true)
            ActionMenuItem.SelectAll -> presenter.setSelectionAll()
            ActionMenuItem.Deselect -> presenter.deselectAll()
            ActionMenuItem.Download -> presenter.createDownloadFile()
            ActionMenuItem.SelectAll -> presenter.selectAll()
            ActionMenuItem.EmptyTrash -> {
                showDeleteDialog(
                    count = -1,
                    tag = DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY
                )
            }
            else -> Unit
        }
    }

    protected fun showExplorerContextBottomDialog(state: ExplorerContextState) {
        ExplorerContextBottomDialog.newInstance(state).also { dialog ->
            dialog.show(parentFragmentManager, ExplorerContextBottomDialog.TAG)
        }
    }
}