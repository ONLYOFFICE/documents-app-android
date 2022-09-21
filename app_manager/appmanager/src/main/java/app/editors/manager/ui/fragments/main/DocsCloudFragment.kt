package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.clearFragmentResultListener
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.filter.FilterType
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.presenters.main.DocsCloudPresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.mvp.views.main.DocsCloudView
import app.editors.manager.ui.activities.main.FilterActivity
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.AddRoomBottomDialog
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.dialogs.MoveCopyDialog
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.BUNDLE_KEY_REFRESH
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.REQUEST_KEY_REFRESH
import app.editors.manager.ui.popup.SelectActionBarPopup
import lib.toolkit.base.managers.utils.TimeUtils.fileTimeStamp
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.UiUtils.setMenuItemTint
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import lib.toolkit.base.ui.popup.ActionBarPopupItem
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
                BaseActivity.REQUEST_ACTIVITY_WEB_VIEWER -> {
                    showViewerActivity(data!!.getSerializableExtra("TAG_FILE") as CloudFile?)
                }
                BaseActivity.REQUEST_ACTIVITY_OPERATION -> {
                    showSnackBar(R.string.operation_complete_message)
                    onRefresh()
                }
                BaseActivity.REQUEST_ACTIVITY_STORAGE -> {
                    val folder = data?.getSerializableExtra(StorageActivity.TAG_RESULT) as CloudFolder?
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
                BaseActivity.REQUEST_ACTIVITY_FILE_PICKER -> {
                    data?.clipData?.let {
                        cloudPresenter.upload(null, it)
                    }
                    data?.data?.let {
                        cloudPresenter.upload(it, null)
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

    override fun showMainActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showMainActionBarMenu(excluded)
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
                if (ApiContract.SectionType.isRoom(section)) {
                    menuInflater.inflate(R.menu.docs_select_room, menu)
                    menu.findItem(R.id.toolbar_selection_archive).setVisible(true).also {
                            setMenuItemTint(requireContext(), it, lib.toolkit.base.R.color.colorPrimary)
                        }
                } else {
                    menuInflater.inflate(R.menu.docs_select, menu)
                    deleteItem = menu.findItem(R.id.toolbar_selection_delete)
                        .setVisible(cloudPresenter.isContextItemEditable).also {
                            setMenuItemTint(requireContext(), it, lib.toolkit.base.R.color.colorPrimary)
                        }
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
        super.onActionButtonClick(buttons)
        when (buttons) {
            ActionBottomDialog.Buttons.PHOTO -> if (checkCameraPermission()) {
                showCameraActivity(fileTimeStamp)
            }
            ActionBottomDialog.Buttons.STORAGE -> {
                showStorageActivity(cloudPresenter.isUserSection)
            }
            else -> {}
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

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        super.onContextButtonClick(buttons)
        when (buttons) {
            ContextBottomDialog.Buttons.RESTORE -> presenter.moveContext()
            ContextBottomDialog.Buttons.EDIT -> cloudPresenter.onEditContextClick()
            ContextBottomDialog.Buttons.SHARE -> showShareActivity(
                cloudPresenter.itemClicked
            )
            ContextBottomDialog.Buttons.EXTERNAL -> {
                setContextDialogExternalLinkEnable(false)
                cloudPresenter.saveExternalLinkToClipboard()
            }
            ContextBottomDialog.Buttons.SHARE_DELETE -> showQuestionDialog(
                getString(R.string.dialogs_question_share_remove),
                cloudPresenter.itemTitle,
                getString(R.string.dialogs_question_share_remove),
                getString(R.string.dialogs_common_cancel_button),
                DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE
            )
            ContextBottomDialog.Buttons.FAVORITE -> cloudPresenter.addToFavorite()
            ContextBottomDialog.Buttons.OPEN_LOCATION -> cloudPresenter.openLocation()
            ContextBottomDialog.Buttons.ARCHIVE -> {
                cloudPresenter.archiveRoom()
            }
            ContextBottomDialog.Buttons.PIN -> {
                cloudPresenter.pinRoom()
            }
            else -> {}
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

    override fun showSelectedActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showSelectedActionBarMenu(mutableListOf<ActionBarPopupItem>().apply {
            if (!cloudPresenter.isContextItemEditable) add(SelectActionBarPopup.Move)
            if (!cloudPresenter.isTrashMode) add(SelectActionBarPopup.Restore)
            if (cloudPresenter.isTrashMode) add(SelectActionBarPopup.Download)
        })
    }

    override fun onFileWebView(file: CloudFile) {
        showViewerActivity(file)
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
        setMenuFilterEnabled(!ApiContract.SectionType.isRoom(section))
    }


    override fun onDocsRefresh(list: List<Entity>?) {
        super.onDocsRefresh(list)
        setMenuFilterEnabled(!ApiContract.SectionType.isRoom(section))
    }

    override fun onDocsFilter(list: List<Entity>?) {
        super.onDocsFilter(list)
        setMenuFilterEnabled(!ApiContract.SectionType.isRoom(section))
    }

    private fun setMenuFilterEnabled(isEnabled: Boolean) {
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

    override fun onActionDialog(isThirdParty: Boolean, isDocs: Boolean) {
        if (cloudPresenter.isCurrentRoom) {
            AddRoomBottomDialog().apply {
                onClickListener = object : AddRoomBottomDialog.OnClickListener {
                    override fun onActionButtonClick(roomType: Int) {
                        UiUtils.showEditDialog(
                            requireContext(),
                            "Room title",
                            value = "New room",
                            acceptListener = { title ->
                                cloudPresenter.createRoom(title, roomType)
                            },
                            requireValue = true

                        )
                    }

                    override fun onActionDialogClose() {
                       this@DocsCloudFragment.onActionDialogClose()
                    }

                }
            }.show(parentFragmentManager, AddRoomBottomDialog.TAG)
        } else {
            super.onActionDialog(isThirdParty, isDocs)
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
        super.onStateEmptyBackStack()
        swipeRefreshLayout?.isRefreshing = true
        cloudPresenter.getItemsById(arguments?.getString(KEY_PATH))
    }

    private fun getFilters(): Boolean {
        val filter = presenter.preferenceTool.filter
        return filter.type != FilterType.None || filter.author.id.isNotEmpty() || filter.excludeSubfolder
    }

    private fun showFilter() {
        if (isTablet) {
            FilterDialogFragment.newInstance(presenter.folderId)
                .show(requireActivity().supportFragmentManager, FilterDialogFragment.TAG)
            requireActivity().supportFragmentManager
                .setFragmentResultListener(REQUEST_KEY_REFRESH, this) { _, bundle ->
                    if (bundle.getBoolean(BUNDLE_KEY_REFRESH, true)) {
                        presenter.refresh()
                        clearFragmentResultListener(REQUEST_KEY_REFRESH)
                    }
                }
        } else {
            filterActivity.launch(FilterActivity.getIntent(this, presenter.folderId))
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