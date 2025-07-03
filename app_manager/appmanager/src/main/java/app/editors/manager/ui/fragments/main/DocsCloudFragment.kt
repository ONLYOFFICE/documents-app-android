package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.ExportIndexOperation
import app.documents.core.network.manager.models.explorer.Lifetime
import app.editors.manager.R
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.tools.ActionMenuItem
import app.editors.manager.mvp.models.filter.FilterType
import app.editors.manager.mvp.models.list.RecentViaLink
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.main.DocsCloudPresenter
import app.editors.manager.mvp.views.main.DocsCloudView
import app.editors.manager.ui.activities.main.FilterActivity
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.AddRoomBottomDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.BUNDLE_KEY_REFRESH
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.REQUEST_KEY_REFRESH
import app.editors.manager.ui.dialogs.fragments.FormCompletedDialogFragment
import app.editors.manager.ui.dialogs.fragments.OperationDialogFragment
import app.editors.manager.ui.fragments.main.DocsRoomFragment.Companion.KEY_RESULT_ROOM_ID
import app.editors.manager.ui.fragments.main.DocsRoomFragment.Companion.KEY_RESULT_ROOM_TYPE
import app.editors.manager.ui.fragments.main.DocsRoomFragment.Companion.TAG_PROTECTED_ROOM_SHOW_INFO
import app.editors.manager.ui.fragments.main.versionhistory.RefreshListener
import app.editors.manager.ui.fragments.main.versionhistory.VersionHistoryFragment
import app.editors.manager.ui.fragments.room.add.AddRoomFragment
import app.editors.manager.ui.fragments.room.add.EditRoomFragment
import app.editors.manager.ui.fragments.share.SetRoomOwnerFragment
import app.editors.manager.ui.fragments.share.ShareFragment
import app.editors.manager.ui.fragments.share.link.RoomInfoFragment
import app.editors.manager.ui.fragments.share.link.ShareSettingsFragment
import app.editors.manager.ui.fragments.template.createroom.RoomFromTemplateFragment
import app.editors.manager.ui.fragments.template.info.TemplateInfoFragment
import app.editors.manager.ui.fragments.template.settings.TemplateAccessSettingsFragment
import app.editors.manager.ui.fragments.template.settings.TemplateSettingsFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.viewModels.main.CopyItems
import app.editors.manager.viewModels.main.TemplateSettingsMode
import app.editors.manager.viewModels.main.VersionViewer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import lib.toolkit.base.managers.tools.FileExtensions
import lib.toolkit.base.managers.utils.DialogUtils
import lib.toolkit.base.managers.utils.EditType
import lib.toolkit.base.managers.utils.EditorsContract
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint
import lib.toolkit.base.managers.utils.contains
import lib.toolkit.base.managers.utils.getSerializable
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

sealed interface ToolbarState {
    data class RoomLifetime(val lifetime: Lifetime) : ToolbarState
    data object RoomTemplate : ToolbarState
    data object None : ToolbarState
}

open class DocsCloudFragment : DocsBaseFragment(), DocsCloudView {

    @InjectPresenter
    override lateinit var presenter: DocsCloudPresenter

    @ProvidePresenter
    fun providePresenter(): DocsCloudPresenter {
        val account = getApp().appComponent.accountOnline
        return account?.let { DocsCloudPresenter(it) }
            ?: run {
                (requireActivity() as IMainActivity).onLogOut()
                DocsCloudPresenter(CloudAccount(id = ""))
            }
    }

    private val filterActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onRefresh()
        }
    }

    private var refreshListener: RefreshListener? = null

    override fun onEditorActivityResult(resultCode: Int, data: Intent?) {
        super.onEditorActivityResult(resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (data?.data != null) {
                val isSendForm = data.getBooleanExtra(EditorsContract.EXTRA_IS_SEND_FORM, false)
                val fillSession = data.getStringExtra(EditorsContract.EXTRA_FILL_SESSION)
                if (isSendForm) {
                    val clickedFile = presenter.itemClicked as? CloudFile
                    if (clickedFile?.formFillingStatus != ApiContract.FormFillingStatus.None) {
                        showFillingStatusFragment(true)
                    } else if (fillSession != null) {
                        showFillResultFragment(fillSession)
                    }
                }
            }
            refreshAfterEditing()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BaseActivity.REQUEST_ACTIVITY_STORAGE -> {
                    val folder = data?.getSerializable(StorageActivity.TAG_RESULT, CloudFolder::class.java)
                    val layoutManager = this@DocsCloudFragment.layoutManager
                    if (layoutManager is LinearLayoutManager) {
                        presenter.addFolderAndOpen(folder, layoutManager.findFirstVisibleItemPosition())
                    }
                }
                BaseActivity.REQUEST_ACTIVITY_SHARE -> {
                    presenter.refresh()
                }
                FilterActivity.REQUEST_ACTIVITY_FILTERS_CHANGED -> {
                    onRefresh()
                }
            }
        }
    }

    private fun refreshAfterEditing(){
        if (refreshListener != null){
            refreshListener!!.refresh()
        } else {
            presenter.refreshWithDelay()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onActionBarTitle(title: String) {
        if (isActivePage) {
            setActionBarTitle(title)
            if (title == "0") {
                disableMenu()
            }
        }
    }

    override fun onStateMenuSelection() {
        menu?.let { menu ->
            menuInflater?.let { menuInflater ->
                menuInflater.inflate(R.menu.docs_select, menu)
                deleteItem = menu.findItem(R.id.toolbar_selection_delete)
                    .setVisible(presenter.isContextItemEditable && presenter.areItemsRemovable).also {
                        setMenuItemTint(requireContext(), it, lib.toolkit.base.R.color.colorPrimary)
                    }
                setAccountEnable(false)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return if (presenter.interruptConversion()) true else super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_item_filter -> showFilter()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showMoveCopyDialog(names: ArrayList<String>, action: String, title: String) {
        moveCopyDialog = MoveCopyDialog.newInstance(names, action, title)
        moveCopyDialog?.dialogButtonOnClick = this
        moveCopyDialog?.show(parentFragmentManager, MoveCopyDialog.TAG)
    }

    override fun onActionButtonClick(buttons: ActionBottomDialog.Buttons?) {
        if (!isVisible) return
        when (buttons) {
            ActionBottomDialog.Buttons.STORAGE -> {
                showStorageActivity(presenter.isUserSection)
            }

            else -> {
                super.onActionButtonClick(buttons)
            }
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        if (!isVisible) return
        super.onAcceptClick(dialogs, value, tag)
        tag?.let {
            when (tag) {
                DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY -> presenter.emptyTrash()
                DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE -> presenter.removeShareContext()
            }
        }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        if (!isVisible) return
        when (tag) {
            DocsBasePresenter.TAG_DIALOG_CANCEL_CONVERSION -> presenter.interruptConversion()
            else -> super.onCancelClick(dialogs, tag)
        }
    }

    override fun onContextButtonClick(contextItem: ExplorerContextItem) {
        if (!isVisible) return
        when (contextItem) {
            ExplorerContextItem.Share -> showShareFragment()
            ExplorerContextItem.Location -> presenter.openLocation()
            ExplorerContextItem.CreateRoom -> {
                if (presenter.isTemplatesFolder) {
                    presenter.createRoomFromTemplate()
                } else {
                    showAddRoomBottomDialog()
                }
            }
            ExplorerContextItem.ShareDelete -> showQuestionDialog(
                title = getString(R.string.dialogs_question_share_remove),
                string = "${presenter.itemClicked?.title}",
                acceptButton = getString(R.string.dialogs_question_accept_remove),
                cancelButton = getString(R.string.dialogs_common_cancel_button),
                tag = DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE,
                acceptErrorTint = true
            )
            is ExplorerContextItem.Fill -> presenter.openFillFormFile()
            is ExplorerContextItem.ExternalLink -> presenter.saveExternalLinkToClipboard()
            is ExplorerContextItem.Restore -> presenter.moveCopySelected(OperationType.RESTORE)
            is ExplorerContextItem.Favorites -> presenter.addToFavorite()
            is ExplorerContextItem.VersionHistory -> presenter.showVersionHistory()
            is ExplorerContextItem.FillingStatus -> showFillingStatusFragment(false)
            is ExplorerContextItem.StopFilling -> showStopFillingQuestionDialog()
            else -> super.onContextButtonClick(contextItem)
        }
    }

    override val actionMenuClickListener: (ActionMenuItem) -> Unit = { item ->
        when (item) {
            is ActionMenuItem.CopyLink -> presenter.copyLinkFromActionMenu(item.isRoom)
            ActionMenuItem.CreateRoom -> {
                if (presenter.currentFolder?.isTemplate == true) {
                    presenter.createRoomFromTemplate()
                } else {
                    showAddRoomBottomDialog()
                }
            }
            else -> super.actionMenuClickListener(item)
        }
    }

    private fun showFillingStatusFragment(isSendForm: Boolean) {
        FillingStatusFragment.show(
            activity = requireActivity(),
            file = presenter.itemClicked as? CloudFile ?: return,
            isSendForm = isSendForm,
            onClose = { presenter.refresh() },
            onStartFill = { presenter.openFile(EditType.Fill(), false) }
        )
    }

    private fun showStopFillingQuestionDialog() {
        UiUtils.showQuestionDialog(
            context = requireContext(),
            title = getString(R.string.filling_form_stop_filling_dialog_title),
            description = getString(R.string.filling_form_stop_filling_dialog_desc),
            acceptTitle = getString(R.string.filling_form_stop_filling),
            acceptListener = { presenter.stopFillingForm() },
        )
    }

    private fun showShareFragment() {
        presenter.itemClicked?.let { item ->
            if (requireContext().accountOnline.isDocSpace && item is CloudFile) {
                ShareSettingsFragment.show(requireActivity(), item.id, item.fileExst)
            } else {
                ShareFragment.show(
                    activity = requireActivity(),
                    itemId = item.id,
                    isFolder = item is CloudFolder
                )
            }
        }
    }

    override fun continueClick(tag: String?, action: String?) {
        var operationType = ApiContract.Operation.OVERWRITE
        when (tag) {
            MoveCopyDialog.TAG_DUPLICATE -> operationType = ApiContract.Operation.DUPLICATE
            MoveCopyDialog.TAG_OVERWRITE -> operationType = ApiContract.Operation.OVERWRITE
            MoveCopyDialog.TAG_SKIP -> operationType = ApiContract.Operation.SKIP
        }
        presenter.transfer(operationType, action != MoveCopyDialog.ACTION_COPY)
    }

    override fun onFileWebView(file: CloudFile, isEditMode: Boolean) {
        if (requireActivity() is IMainActivity) {
            (requireActivity() as IMainActivity).showWebViewer(file, isEditMode)
        }
    }

    /*
     * On pager scroll callback
     * */
    override fun onScrollPage() {
        presenter.initViews()
        if (presenter.stack == null) {
            presenter.getItemsById(arguments?.getString(KEY_PATH))
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.setSectionType(section)
        onStateUpdateFilterMenu()
    }

    override fun onStateMenuEnabled(isEnabled: Boolean) {
        super.onStateMenuEnabled(isEnabled)
        setMenuFilterEnabled(isEnabled)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.setSectionType(section)
    }

    override fun onDocsGet(list: List<Entity>?) {
        val newList = list.orEmpty().toMutableList()
        if (presenter.getSectionType() == ApiContract.SectionType.CLOUD_USER &&
            context?.accountOnline.isDocSpace &&
            presenter.isRoot
        ) {
            newList.add(0, RecentViaLink)
        }
        super.onDocsGet(newList)
        setMenuFilterEnabled(true)
    }

    override fun onDocsRefresh(list: List<Entity>?) {
        val newList = list.orEmpty().toMutableList()
        if (presenter.getSectionType() == ApiContract.SectionType.CLOUD_USER &&
            context?.accountOnline.isDocSpace &&
            presenter.isRoot
        ) {
            newList.add(0, RecentViaLink)
        }
        super.onDocsRefresh(newList)
        setMenuFilterEnabled(true)
    }

    override fun onDocsNext(list: List<Entity>?) {
        val newList = list.orEmpty().toMutableList()
        if (presenter.getSectionType() == ApiContract.SectionType.CLOUD_USER &&
            context?.accountOnline.isDocSpace &&
            presenter.isRoot
        ) {
            newList.add(0, RecentViaLink)
        }
        super.onDocsNext(newList)
    }

    override fun onDocsFilter(list: List<Entity>?) {
        super.onDocsFilter(list)
        setMenuFilterEnabled(true)
    }

    protected open fun setMenuFilterEnabled(isEnabled: Boolean) {
        filterItem?.isVisible = true
        filterItem?.isEnabled = isEnabled
        onStateUpdateFilterMenu()
    }

    override fun onStateUpdateFilterMenu() {
        filterItem?.icon = if (getFilters()) {
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_toolbar_filter_enable)
        } else {
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_toolbar_filter_disable)
        }
    }

    override val isActivePage: Boolean
        get() = isResumed.or(super.isActivePage)

    protected val section: Int
        get() = arguments?.getInt(KEY_SECTION) ?: ApiContract.SectionType.UNKNOWN

    override fun onSwipeRefresh(): Boolean {
        if (!super.onSwipeRefresh()) {
            presenter.getItemsById(arguments?.getString(KEY_PATH))
            return true
        }
        return false
    }

    override fun onStateEmptyBackStack() {
        swipeRefreshLayout?.isRefreshing = false
        presenter.getItemsById(arguments?.getString(KEY_PATH))
    }

    override fun onPlaceholder(type: PlaceholderViews.Type) {
        val placeholder = if (type == PlaceholderViews.Type.EMPTY) {
            val roomType = presenter.currentFolder?.roomType
            when {
                roomType != null && roomType > 0 -> {
                    when {
                        presenter.currentFolder?.isTemplate == true -> PlaceholderViews.Type.EMPTY_TEMPLATE
                        presenter.itemClicked?.security?.editRoom != true -> PlaceholderViews.Type.VISITOR_EMPTY_ROOM
                        roomType == ApiContract.RoomType.FILL_FORMS_ROOM -> PlaceholderViews.Type.EMPTY_FORM_FILLING_ROOM
                        roomType == ApiContract.RoomType.VIRTUAL_ROOM -> PlaceholderViews.Type.EMPTY_VIRTUAL_ROOM
                        else -> PlaceholderViews.Type.EMPTY_ROOM
                    }
                }
                presenter.isTemplatesFolder -> PlaceholderViews.Type.EMPTY_TEMPLATES_FOLDER
                presenter.isRecentViaLinkSection() -> PlaceholderViews.Type.EMPTY_RECENT_VIA_LINK
                else -> type
            }
        } else type
        super.onPlaceholder(placeholder)
    }

    override fun onUpdateItemState() {
        if (section == ApiContract.SectionType.CLOUD_FAVORITES) explorerAdapter?.removeItem(presenter.itemClicked)
        else super.onUpdateItemState()
    }

    override fun onConversionQuestion() {
        (presenter.itemClicked as? CloudFile)?.let { file ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.conversion_dialog_title)
                .setMessage(R.string.conversion_dialog_message)
                .setPositiveButton(
                    getString(
                        R.string.conversion_dialog_convert_to,
                        FileExtensions.toOOXML(file.fileExst)
                    )
                ) { dialog, _ ->
                    dialog.dismiss()
                    presenter.convertToOOXML()
                }
                .setNegativeButton(R.string.conversion_dialog_open_in_view_mode) { dialog, _ ->
                    dialog.dismiss()
                    presenter.openFile(EditType.View())
                }
                .create()
                .apply {
                    window?.setLayout(
                        DialogUtils.getWidth(requireContext()),
                        WindowManager.LayoutParams.WRAP_CONTENT
                    )
                }
                .show()
        }
    }

    override fun onConversionProgress(progress: Int, extension: String?) {
        if (progress > 0) {
            updateProgressDialog(100, progress)
        } else {
            showProgressDialog(
                title = getString(R.string.conversion_dialog_converting_to, extension),
                isHideButton = false,
                cancelTitle = getString(R.string.dialogs_common_cancel_button),
                tag = DocsBasePresenter.TAG_DIALOG_CANCEL_CONVERSION
            )
            updateProgressDialog(100, 0)
        }
    }

    protected open fun getFilters(): Boolean {
        val filter = presenter.preferenceTool.filter
        return filter.type != FilterType.None || filter.author.id.isNotEmpty() || filter.excludeSubfolder
    }

    private fun init() {
        explorerAdapter?.isSectionMy = section == ApiContract.SectionType.CLOUD_USER
        presenter.checkBackStack()
    }

    private fun disableMenu() {
        menu?.let {
            deleteItem?.isEnabled = false
        }
    }

    private fun showFilter() {
        val filterSection = when {
            presenter.isRecentViaLinkSection() -> ApiContract.SectionType.CLOUD_RECENT
            presenter.isTemplatesFolder -> ApiContract.SectionType.CLOUD_TEMPLATES
            else -> section
        }
        if (isTablet) {
            FilterDialogFragment.newInstance(
                presenter.folderId,
                filterSection,
                presenter.isRoot
            ).show(requireActivity().supportFragmentManager, FilterDialogFragment.TAG)

            requireActivity().supportFragmentManager
                .setFragmentResultListener(REQUEST_KEY_REFRESH, this) { _, bundle ->
                    if (bundle.getBoolean(BUNDLE_KEY_REFRESH, true)) {
                        presenter.refresh()
                        clearFragmentResultListener(REQUEST_KEY_REFRESH)
                    }
                }
        } else {
            filterActivity.launch(FilterActivity.getIntent(
                this,
                presenter.folderId,
                filterSection,
                presenter.isRoot
            ))
        }
    }

    private fun openRoom(id: String?, type: Int? = null, popToRoot: Boolean = false) {
        try {
            if (popToRoot) presenter.popToRoot()
            requireActivity().supportFragmentManager
                .fragments
                .filterIsInstance<IMainPagerFragment>()
                .first()
                .setPagerPosition(ApiContract.SectionType.CLOUD_VIRTUAL_ROOM) {
                    setFragmentResult(KEY_ROOM_CREATED_REQUEST, bundleOf(KEY_RESULT_ROOM_ID to id, KEY_RESULT_ROOM_TYPE to type))
                }
        } catch (_: NoSuchElementException) {
        }
    }

    override fun onLeaveRoomDialog(title: Int, question: Int, isOwner: Boolean) {
        showQuestionDialog(
            title = getString(title),
            string = getString(question),
            acceptButton = if (isOwner) getString(R.string.leave_room_assign) else getString(R.string.dialogs_question_accept_yes),
            cancelButton = getString(R.string.dialogs_common_cancel_button),
            tag = DocsRoomFragment.TAG_LEAVE_ROOM
        )
    }

    override fun showSetOwnerFragment(cloudFolder: CloudFolder) {
        hideDialog()
        SetRoomOwnerFragment.show(cloudFolder, requireActivity(), presenter::refresh)
    }

    override fun showAddRoomFragment(type: Int, copyItems: CopyItems?) {
        AddRoomFragment.show(
            activity = requireActivity(),
            type = type,
            copyItems = copyItems
        ) { bundle ->
            if (bundle.contains("id")) {
                openRoom(id = bundle.getString("id"), type = bundle.getInt("type"))
            } else {
                onRefresh()
            }
        }
    }

    override fun showEditRoomFragment(room: CloudFolder) {
        EditRoomFragment.show(activity = requireActivity(), room.id) { onRefresh() }
    }

    override fun showFillFormChooserFragment() {
        FillFormChooserFragment.show(
            activity = requireActivity(),
            onFillForm = { presenter.openFile(EditType.Fill()) },
            onSelectRoom = { presenter.moveCopyOperation(OperationType.COPY_TO_FILL_FORM_ROOM) }
        )
    }

    override fun setToolbarState(state: ToolbarState) {
        (activity as? IMainActivity)?.let { activity ->
            when (state) {
                is ToolbarState.RoomLifetime -> {
                    activity.setToolbarInfo(
                        title = getString(
                            R.string.rooms_vdr_lifetime_info,
                            state.lifetime.value,
                            when (state.lifetime.period) {
                                Lifetime.PERIOD_DAYS -> lib.toolkit.base.R.plurals.days
                                Lifetime.PERIOD_MONTHS -> lib.toolkit.base.R.plurals.months
                                Lifetime.PERIOD_YEARS -> lib.toolkit.base.R.plurals.years
                                else -> return@let
                            }.let { resources.getQuantityText(it, state.lifetime.value) }
                        ),
                        drawable = lib.toolkit.base.R.drawable.ic_expiring,
                    )
                }

                is ToolbarState.RoomTemplate -> {
                    activity.setToolbarInfo(
                        title = getString(R.string.template_subheader_title),
                        drawable = R.drawable.ic_template_subheader,
                        drawablePadding = 12
                    )
                }

                ToolbarState.None -> activity.setToolbarInfo(null)
            }
        }
    }

    override fun onRoomFileIndexing(indexing: Boolean) {
        explorerAdapter?.isIndexing = indexing
    }

    override fun onRoomExportIndex(operation: ExportIndexOperation) {
        UiUtils.showQuestionDialog(
            requireContext(),
            title = getString(R.string.rooms_index_reorder_complete_title),
            description = getString(R.string.rooms_index_reorder_complete_desc, operation.resultFileName),
            cancelTitle = getString(R.string.dialogs_common_close),
            acceptTitle = getString(R.string.rooms_index_reorder_open_file),
            acceptListener = { presenter.openFileById(operation.resultFileId, EditType.Edit()) }
        )
    }

    override fun onBatchMoveCopy(operation: OperationType, explorer: Explorer) {
        OperationDialogFragment.show(requireActivity(), operation, explorer) { bundle ->
            if (OperationDialogFragment.KEY_OPERATION_RESULT_COMPLETE in bundle) {
                showSnackBar(R.string.operation_complete_message)
                presenter.getBackStack()
            } else if (OperationDialogFragment.KEY_OPERATION_RESULT_OPEN_FOLDER in bundle) {
                openRoom(bundle.getString(OperationDialogFragment.KEY_OPERATION_RESULT_OPEN_FOLDER))
            }
        }
    }

    override fun onRoomViaLinkPasswordRequired(error: Boolean, tag: String) { }

    override fun showRoomInfoFragment() {
        if (presenter.isTemplatesFolder || presenter.currentFolder?.isTemplate == true) {
            presenter.showTemplateInfo()
            return
        }
        if (presenter.roomClicked?.passwordProtected == true) {
            onRoomViaLinkPasswordRequired(false, TAG_PROTECTED_ROOM_SHOW_INFO)
            return
        }
        RoomInfoFragment.newInstance(presenter.roomClicked ?: error("room can not be null"))
            .show(requireActivity().supportFragmentManager, RoomInfoFragment.TAG)
    }

    private fun showFillResultFragment(stringExtra: String?) {
        stringExtra?.let {
            FormCompletedDialogFragment.show(parentFragmentManager, stringExtra)
            parentFragmentManager.setFragmentResultListener(FormCompletedDialogFragment.KEY_RESULT, this) { _, data ->
                if (data.getString("id")?.isNotEmpty() == true) {
                    presenter.getItemsById(data.getString("id"))
                }
                parentFragmentManager.clearFragmentResult(FormCompletedDialogFragment.KEY_RESULT)
            }
        }
    }

    protected fun showAddRoomBottomDialog(copyFiles: Boolean = true) {
        setFragmentResultListener(AddRoomBottomDialog.KEY_REQUEST_TYPE) { _, bundle ->
            onActionDialogClose()
            if (bundle.contains(AddRoomBottomDialog.KEY_RESULT_TYPE)) {
                val roomType = bundle.getInt(AddRoomBottomDialog.KEY_RESULT_TYPE)
                if (roomType == 0) {
                    showRoomFromTemplateFragment(null)
                } else if (copyFiles) {
                    presenter.createRoom(roomType)
                } else {
                    showAddRoomFragment(roomType)
                }
            }
        }
        AddRoomBottomDialog().show(parentFragmentManager, AddRoomBottomDialog.TAG)
    }

    override fun showFillFormIncompatibleVersionsDialog() {
        UiUtils.showQuestionDialog(
            context = requireContext(),
            title = getString(R.string.rooms_filter_type_filling_forms),
            description = getString(R.string.filling_form_version_incompatible_desc),
            acceptTitle = getString(R.string.conversion_dialog_open_in_view_mode),
            acceptListener = { presenter.openFile(EditType.View()) },
        )
    }

    override fun showVersionHistoryFragment(fileId: String) {
        refreshListener = VersionHistoryFragment.show(
            parentFragmentManager,
            viewLifecycleOwner,
            fileId,
            presenter as VersionViewer
        ) {
            onRefresh()
            refreshListener = null
        }
    }

    override fun showTemplateSettingsFragment(templateId: String, modeId: Int) {
        TemplateSettingsFragment.show(
            parentFragmentManager,
            viewLifecycleOwner,
            templateId,
            modeId
        ) { bundle ->
            val id = bundle.getString(TemplateSettingsFragment.KEY_SAVED_ID)
            val type = bundle.getInt(TemplateSettingsFragment.KEY_SAVED_ROOM_TYPE)
            val title = bundle.getString(TemplateSettingsFragment.KEY_SAVED_TITLE)
            when (modeId) {
                TemplateSettingsMode.MODE_CREATE_TEMPLATE -> {
                    id?.let {
                        openRoom(id = id, type = type)
                        showSnackBar(getString(R.string.template_created_successfully, title))
                    }
                }

                TemplateSettingsMode.MODE_EDIT_TEMPLATE -> {
                    onRefresh()
                    showSnackBar(R.string.settings_saved_successfully)
                }

                TemplateSettingsMode.MODE_CREATE_ROOM -> {
                    id?.let {
                        openRoom(id = id, type = type, popToRoot = true)
                        showSnackBar(R.string.room_created_successfully)
                    }
                }
            }

        }
    }

    override fun showTemplateAccessSettingsFragment(templateId: String) {
        TemplateAccessSettingsFragment.show(
            parentFragmentManager,
            viewLifecycleOwner,
            templateId
        ) {
            showSnackBar(R.string.settings_saved_successfully)
        }
    }

    override fun showRoomFromTemplateFragment(templateId: String?) {
        RoomFromTemplateFragment.show(
            parentFragmentManager,
            viewLifecycleOwner,
            templateId
        ) { bundle ->
            bundle.getString(RoomFromTemplateFragment.KEY_SAVED_ID)?.let { id ->
                val roomType = bundle.getInt(RoomFromTemplateFragment.KEY_SAVED_ROOM_TYPE)
                openRoom(id, roomType, popToRoot = true)
                showSnackBar(R.string.room_created_successfully)
            }
        }
    }

    override fun showTemplateInfoFragment(templateId: String) {
        TemplateInfoFragment.show(
            parentFragmentManager,
            viewLifecycleOwner,
            templateId
        ) { bundle ->
            bundle.getString(TemplateInfoFragment.KEY_SAVED_ID)?.let { id ->
                val roomType = bundle.getInt(TemplateInfoFragment.KEY_SAVED_ROOM_TYPE)
                openRoom(id, roomType, popToRoot = true)
                showSnackBar(R.string.room_created_successfully)
            }
        }
    }

    val isRoot: Boolean
        get() = presenter.isRoot

    companion object {
        const val KEY_SECTION = "section"
        const val KEY_PATH = "path"
        const val KEY_ROOM_CREATED_REQUEST = "key_room_created_result"

        fun newInstance(section: Int, rootPath: String): DocsCloudFragment {
            return DocsCloudFragment().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_PATH, rootPath)
                    putInt(KEY_SECTION, section)
                }
            }
        }
    }
}