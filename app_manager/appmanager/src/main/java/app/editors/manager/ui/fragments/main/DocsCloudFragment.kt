package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.clearFragmentResultListener
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
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
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.BUNDLE_KEY_REFRESH
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.REQUEST_KEY_REFRESH
import app.editors.manager.ui.popup.MainPopupItem
import app.editors.manager.ui.popup.SelectPopupItem
import app.editors.manager.ui.views.custom.PlaceholderViews
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint
import lib.toolkit.base.managers.utils.getSerializable
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

open class DocsCloudFragment : DocsBaseFragment(), DocsCloudView {

    @InjectPresenter
    lateinit var cloudPresenter: DocsCloudPresenter

    private val filterActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onRefresh()
        }
    }

    @ProvidePresenter
    fun providePresenter(): DocsCloudPresenter {
        val account = getApp().appComponent.accountOnline
        return account?.let { DocsCloudPresenter(it) }
            ?: throw RuntimeException("Cloud account can't be null")
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

    override fun showMoveCopyDialog(names: ArrayList<String>, action: String, titleFolder: String) {
        moveCopyDialog = MoveCopyDialog.newInstance(names, action, titleFolder)
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

    override fun onContextButtonClick(contextItem: ExplorerContextItem) {
        when (contextItem) {
            ExplorerContextItem.Edit -> cloudPresenter.onEditContextClick()
            ExplorerContextItem.Share -> showShareActivity(cloudPresenter.itemClicked)
            ExplorerContextItem.ExternalLink -> cloudPresenter.saveExternalLinkToClipboard()
            ExplorerContextItem.Location -> cloudPresenter.openLocation()
            ExplorerContextItem.RoomInfo -> ShareActivity.show(this, cloudPresenter.itemClicked, true)
            is ExplorerContextItem.Restore -> presenter.moveCopySelected(OperationsState.OperationType.RESTORE)
            is ExplorerContextItem.Favorites -> cloudPresenter.addToFavorite()
            ExplorerContextItem.ShareDelete -> showQuestionDialog(
                title = getString(R.string.dialogs_question_share_remove),
                string = "${cloudPresenter.itemClicked?.title}",
                acceptButton = getString(R.string.dialogs_question_accept_remove),
                cancelButton = getString(R.string.dialogs_common_cancel_button),
                tag = DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE,
                acceptErrorTint = true
            )
            else -> super.onContextButtonClick(contextItem)
        }
        contextBottomDialog?.dismiss()
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

    override fun onFileWebView(file: CloudFile) {
        if (requireActivity() is IMainActivity) {
            (requireActivity() as IMainActivity).showWebViewer(file)
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

    private fun disableMenu() {
        menu?.let {
            deleteItem?.isEnabled = false
        }
    }

    private fun init() {
        explorerAdapter?.isSectionMy = section == ApiContract.SectionType.CLOUD_USER
        cloudPresenter.checkBackStack()
    }

    override fun onSwipeRefresh(): Boolean {
        if (!super.onSwipeRefresh()) {
            cloudPresenter.getItemsById(arguments?.getString(KEY_PATH))
            return true
        }
        return false
    }

    override fun onStateEmptyBackStack() {
        swipeRefreshLayout?.isRefreshing = true
        cloudPresenter.getItemsById(arguments?.getString(KEY_PATH))
    }

    override fun onArchiveRoom(isArchived: Boolean) {
        if (isArchived) {
            onSnackBar(getString(R.string.context_room_archive_message))
        } else {
            onSnackBar(getString(R.string.context_room_unarchive_message))
        }
        explorerAdapter?.removeItem(presenter.itemClicked)
        if (explorerAdapter?.itemList?.none { it !is Header } == true) {
            onPlaceholder(PlaceholderViews.Type.EMPTY)
        }
    }

    override fun onArchiveSelectedRooms(rooms: List<Entity>) {
        onSnackBar(getString(R.string.context_rooms_archive_message))
        rooms.forEach { explorerAdapter?.removeItem(it) }
        if (explorerAdapter?.itemList?.none { it !is Header } == true) {
            onPlaceholder(PlaceholderViews.Type.EMPTY)
        }
    }

    override fun onUpdateFavoriteItem() {
        if (section == ApiContract.SectionType.CLOUD_FAVORITES) explorerAdapter?.removeItem(presenter.itemClicked)
        else super.onUpdateFavoriteItem()
    }

    protected open fun getFilters(): Boolean {
        val filter = presenter.preferenceTool.filter
        return filter.type != FilterType.None || filter.author.id.isNotEmpty() || filter.excludeSubfolder
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

    val isRoot: Boolean
        get() = presenter.isRoot

    companion object {
        const val KEY_SECTION = "section"
        const val KEY_PATH = "path"
        const val KEY_ACCOUNT = "key_account"

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