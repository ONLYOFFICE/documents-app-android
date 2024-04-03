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
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.app.accountOnline
import app.editors.manager.mvp.models.filter.FilterType
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.main.DocsCloudPresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.mvp.views.main.DocsCloudView
import app.editors.manager.ui.activities.main.FilterActivity
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.dialogs.fragments.AddRoomDialog
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.BUNDLE_KEY_REFRESH
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.REQUEST_KEY_REFRESH
import app.editors.manager.ui.fragments.share.link.RoomInfoFragment
import app.editors.manager.ui.popup.MainPopupItem
import app.editors.manager.ui.popup.SelectPopupItem
import app.editors.manager.ui.views.custom.PlaceholderViews
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import lib.toolkit.base.OpenMode
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.DialogUtils
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint
import lib.toolkit.base.managers.utils.getSerializable
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

open class DocsCloudFragment : DocsBaseFragment(), DocsCloudView {

    @InjectPresenter
    lateinit var cloudPresenter: DocsCloudPresenter

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BaseActivity.REQUEST_ACTIVITY_STORAGE -> {
                    val folder = data?.getSerializable(StorageActivity.TAG_RESULT, CloudFolder::class.java)
                    cloudPresenter.addFolderAndOpen(folder, linearLayoutManager?.findFirstVisibleItemPosition() ?: -1)
                }

                BaseActivity.REQUEST_ACTIVITY_SHARE -> {
                    if (data?.hasExtra(ShareActivity.TAG_RESULT) == true) {
                        cloudPresenter.setItemsShared(data.getBooleanExtra(ShareActivity.TAG_RESULT, false))
                    }
                }

                BaseActivity.REQUEST_ACTIVITY_CAMERA -> {
                    cameraUri?.let { uri ->
                        cloudPresenter.upload(uri, null)
                    }
                }

                FilterActivity.REQUEST_ACTIVITY_FILTERS_CHANGED -> {
                    onRefresh()
                }
                REQUEST_DOCS, REQUEST_SHEETS, REQUEST_PRESENTATION -> {
                    if (data?.data != null) {
                        if (data.getBooleanExtra("EXTRA_IS_MODIFIED", false)) {
                            cloudPresenter.updateDocument(data.data!!)
                        }
                    }
                }
            }
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
                    .setVisible(cloudPresenter.isContextItemEditable).also {
                        setMenuItemTint(requireContext(), it, lib.toolkit.base.R.color.colorPrimary)
                    }
                setAccountEnable(false)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return if (cloudPresenter.interruptConversion()) true else super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_item_filter -> showFilter()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onUploadFileProgress(progress: Int, id: String) {
        explorerAdapter?.getUploadFileById(id).let { uploadFile ->
            uploadFile?.progress = progress
            explorerAdapter?.updateItem(uploadFile)
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

    override fun showMoveCopyDialog(names: ArrayList<String>, action: String, title: String) {
        moveCopyDialog = MoveCopyDialog.newInstance(names, action, title)
        moveCopyDialog?.dialogButtonOnClick = this
        moveCopyDialog?.show(parentFragmentManager, MoveCopyDialog.TAG)
    }

    override fun onActionButtonClick(buttons: ActionBottomDialog.Buttons?) {
        when (buttons) {
            ActionBottomDialog.Buttons.STORAGE -> {
                showStorageActivity(cloudPresenter.isUserSection)
            }

            else -> {
                super.onActionButtonClick(buttons)
            }
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        tag?.let {
            when (tag) {
                DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY -> cloudPresenter.emptyTrash()
                DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE -> cloudPresenter.removeShareContext()
            }
        }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        when (tag) {
            DocsBasePresenter.TAG_DIALOG_CANCEL_CONVERSION -> cloudPresenter.interruptConversion()
            else -> super.onCancelClick(dialogs, tag)
        }
    }

    override fun onContextButtonClick(contextItem: ExplorerContextItem) {
        when (contextItem) {
            ExplorerContextItem.Preview -> cloudPresenter.onContextOpenFile(OpenMode.READ_ONLY)
            ExplorerContextItem.Share -> showShareActivity(cloudPresenter.itemClicked)
            ExplorerContextItem.Location -> cloudPresenter.openLocation()
            ExplorerContextItem.CreateRoom -> cloudPresenter.createRoomFromFolder()
            ExplorerContextItem.RoomInfo -> showRoomInfoFragment()
            ExplorerContextItem.ShareDelete -> showQuestionDialog(
                title = getString(R.string.dialogs_question_share_remove),
                string = "${cloudPresenter.itemClicked?.title}",
                acceptButton = getString(R.string.dialogs_question_accept_remove),
                cancelButton = getString(R.string.dialogs_common_cancel_button),
                tag = DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE,
                acceptErrorTint = true
            )

            is ExplorerContextItem.Edit -> cloudPresenter.onContextOpenFile(OpenMode.EDIT)
            is ExplorerContextItem.ExternalLink -> cloudPresenter.saveExternalLinkToClipboard()
            is ExplorerContextItem.Restore -> presenter.moveCopySelected(OperationsState.OperationType.RESTORE)
            is ExplorerContextItem.Favorites -> cloudPresenter.addToFavorite()
            else -> super.onContextButtonClick(contextItem)
        }
    }

    override fun continueClick(tag: String?, action: String?) {
        var operationType = ApiContract.Operation.OVERWRITE
        when (tag) {
            MoveCopyDialog.TAG_DUPLICATE -> operationType = ApiContract.Operation.DUPLICATE
            MoveCopyDialog.TAG_OVERWRITE -> operationType = ApiContract.Operation.OVERWRITE
            MoveCopyDialog.TAG_SKIP -> operationType = ApiContract.Operation.SKIP
        }
        cloudPresenter.transfer(operationType, action != MoveCopyDialog.ACTION_COPY)
    }

    override fun showSelectActionPopup(vararg excluded: SelectPopupItem) {
        super.showSelectActionPopup(*excluded.toMutableList().apply {
            if (!cloudPresenter.isContextItemEditable) add(SelectPopupItem.Operation.Move)
        }.toTypedArray())
    }

    override fun showMainActionPopup(vararg excluded: MainPopupItem) {
        if (requireContext().accountOnline?.isPersonal() == true) {
            super.showMainActionPopup(MainPopupItem.SortBy.Author)
        } else super.showMainActionPopup(*excluded)
    }

    override fun onFileWebView(file: CloudFile, isEditMode: Boolean) {
        if (requireActivity() is IMainActivity) {
            (requireActivity() as IMainActivity).showWebViewer(file, isEditMode)
        }
    }

    fun setFileData(fileData: String) {
        cloudPresenter.openFile(fileData)
    }

    /*
     * On pager scroll callback
     * */
    override fun onScrollPage() {
        cloudPresenter.initViews()
        if (cloudPresenter.stack == null) {
            cloudPresenter.getItemsById(arguments?.getString(KEY_PATH))
        }
    }

    override fun onResume() {
        super.onResume()
        cloudPresenter.setSectionType(section)
        onStateUpdateFilterMenu()
    }

    override fun onStateMenuEnabled(isEnabled: Boolean) {
        super.onStateMenuEnabled(isEnabled)
        setMenuFilterEnabled(isEnabled)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cloudPresenter.setSectionType(section)
    }

    override fun onDocsGet(list: List<Entity>?) {
        super.onDocsGet(list)
        setMenuFilterEnabled(true)
    }


    override fun onDocsRefresh(list: List<Entity>?) {
        super.onDocsRefresh(list)
        setMenuFilterEnabled(true)
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

    override val presenter: DocsBasePresenter<out DocsBaseView>
        get() = cloudPresenter

    override val isWebDav: Boolean
        get() = false

    protected val section: Int
        get() = arguments?.getInt(KEY_SECTION) ?: ApiContract.SectionType.UNKNOWN

    override fun onSwipeRefresh(): Boolean {
        if (!super.onSwipeRefresh()) {
            cloudPresenter.getItemsById(arguments?.getString(KEY_PATH))
            return true
        }
        return false
    }

    override fun onStateEmptyBackStack() {
        swipeRefreshLayout?.isRefreshing = false
        cloudPresenter.getItemsById(arguments?.getString(KEY_PATH))
    }

    override fun onArchiveRoom(isArchived: Boolean, count: Int) {
        val message = if (isArchived) {
            if (count > 1) {
                getString(R.string.context_rooms_archive_message)
            } else {
                getString(R.string.context_room_archive_message)
            }
        } else {
            resources.getQuantityString(R.plurals.context_rooms_unarchive_message, count)
        }
        onSnackBar(message)
        explorerAdapter?.removeItem(presenter.itemClicked)
        if (explorerAdapter?.itemList?.none { it !is Header } == true) {
            onPlaceholder(PlaceholderViews.Type.EMPTY)
        }
    }

    override fun onArchiveSelectedRooms(rooms: List<Entity>) {
        val message = if (rooms.size > 1) {
            R.string.context_rooms_archive_message
        } else {
            R.string.context_room_archive_message
        }
        onSnackBar(getString(message))
        rooms.forEach { explorerAdapter?.removeItem(it) }
        if (explorerAdapter?.itemList?.none { it !is Header } == true) {
            onPlaceholder(PlaceholderViews.Type.EMPTY)
        }
    }

    override fun onUpdateFavoriteItem() {
        if (section == ApiContract.SectionType.CLOUD_FAVORITES) explorerAdapter?.removeItem(presenter.itemClicked)
        else super.onUpdateFavoriteItem()
    }

    override fun onConversionQuestion() {
        (presenter.itemClicked as? CloudFile)?.let { file ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.conversion_dialog_title)
                .setMessage(R.string.conversion_dialog_message)
                .setPositiveButton(
                    getString(
                        R.string.conversion_dialog_convert_to,
                        LocalContentTools.toOOXML(file.clearExt)
                    )
                ) { dialog, _ ->
                    dialog.dismiss()
                    cloudPresenter.convertToOOXML()
                }
                .setNegativeButton(R.string.conversion_dialog_open_in_view_mode) { dialog, _ ->
                    dialog.dismiss()
                    cloudPresenter.getFileInfo(OpenMode.READ_ONLY)
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

    override fun onCreateRoom(type: Int, item: Item, isCopy: Boolean) {
        showAddRoomFragment(type, item, isCopy)
    }

    protected fun showAddRoomFragment(type: Int, cloudFolder: Item? = null, isCopy: Boolean = false) {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            AddRoomFragment.TAG_RESULT, this
        ) { _, args ->
            if (cloudFolder != null && !isCopy) {
                onRefresh()
            } else {
                openRoom(id = args.getString("id"))
            }
        }
        AddRoomDialog.newInstance(type, cloudFolder, isCopy)
            .show(requireActivity().supportFragmentManager, AddRoomDialog.TAG)
    }

    protected open fun getFilters(): Boolean {
        val filter = presenter.preferenceTool.filter
        return filter.type != FilterType.None || filter.author.id.isNotEmpty() || filter.excludeSubfolder
    }

    private fun init() {
        explorerAdapter?.isSectionMy = section == ApiContract.SectionType.CLOUD_USER
        cloudPresenter.checkBackStack()
    }

    private fun disableMenu() {
        menu?.let {
            deleteItem?.isEnabled = false
        }
    }

    private fun showRoomInfoFragment() {
        (presenter.itemClicked as? CloudFolder)?.let { room ->
            RoomInfoFragment.newInstance(room)
                .show(requireActivity().supportFragmentManager, RoomInfoFragment.TAG)
        }
    }

    private fun showFilter() {
        if (isTablet) {
            FilterDialogFragment.newInstance(presenter.folderId, section, presenter.isRoot)
                .show(requireActivity().supportFragmentManager, FilterDialogFragment.TAG)

            requireActivity().supportFragmentManager
                .setFragmentResultListener(REQUEST_KEY_REFRESH, this) { _, bundle ->
                    if (bundle.getBoolean(BUNDLE_KEY_REFRESH, true)) {
                        presenter.refresh()
                        clearFragmentResultListener(REQUEST_KEY_REFRESH)
                    }
                }
        } else {
            filterActivity.launch(FilterActivity.getIntent(this, presenter.folderId, section, presenter.isRoot))
        }
    }

    private fun openRoom(id: String?) {
        try {
            requireActivity().supportFragmentManager
                .fragments
                .filterIsInstance<IMainPagerFragment>()
                .first()
                .setPagerPosition(ApiContract.SectionType.CLOUD_VIRTUAL_ROOM) {
                    setFragmentResult(KEY_ROOM_CREATED_REQUEST, bundleOf(DocsRoomFragment.KEY_RESULT_ROOM_ID to id))
                }
        } catch (_: NoSuchElementException) { }
    }

    override fun onLeaveRoomDialog(title: Int, question: Int, tag: String, isOwner: Boolean) {
        showQuestionDialog(
            title = getString(title),
            string = getString(question),
            acceptButton = if (isOwner) getString(R.string.leave_room_assign) else getString(R.string.dialogs_question_accept_yes),
            cancelButton = getString(R.string.dialogs_common_cancel_button),
            tag = tag
        )
    }

    override fun showSetOwnerFragment(cloudFolder: CloudFolder) {
        hideDialog()
        ShareActivity.show(fragment = this, item = cloudFolder, isInfo = false, leave = true)
    }

    val isRoot: Boolean
        get() = presenter.isRoot

    companion object {
        const val KEY_SECTION = "section"
        const val KEY_PATH = "path"
        const val KEY_ACCOUNT = "key_account"
        const val KEY_ROOM_CREATED_REQUEST = "key_room_created_result"

        fun newInstance(stringAccount: String, section: Int, rootPath: String): DocsCloudFragment {
            return DocsCloudFragment().apply {
                arguments = Bundle(3).apply {
                    putString(KEY_ACCOUNT, stringAccount)
                    putString(KEY_PATH, rootPath)
                    putInt(KEY_SECTION, section)
                }
            }
        }
    }
}