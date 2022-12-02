package app.editors.manager.ui.fragments.main

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import app.documents.core.account.Recent
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.presenters.main.DocsRecentPresenter
import app.editors.manager.mvp.presenters.main.OpenState
import app.editors.manager.mvp.presenters.main.RecentState
import app.editors.manager.mvp.views.main.DocsRecentView
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.adapters.RecentAdapter
import app.editors.manager.ui.adapters.holders.factory.RecentHolderFactory
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.popup.MainActionBarPopup
import app.editors.manager.ui.views.custom.PlaceholderViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.LaunchActivityForResult
import lib.toolkit.base.managers.utils.RequestPermissions
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.popup.ActionBarPopupItem
import moxy.presenter.InjectPresenter

class DocsRecentFragment : DocsBaseFragment(), DocsRecentView {

    @InjectPresenter
    override lateinit var presenter: DocsRecentPresenter

    private var activity: IMainActivity? = null
    private var adapter: RecentAdapter? = null
    private var filterValue: CharSequence? = null
    private var clearItem: MenuItem? = null

    private val recentListener: (recent: Recent, position: Int) -> Unit = { recent, position ->
        Debounce.perform(1000L) { presenter.fileClick(recent, position) }
    }

    private val contextListener: (recent: Recent, position: Int) -> Unit = { recent, position ->
        presenter.contextClick(recent, position)
    }

    private val readStorage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        presenter.recreateStack()
        presenter.getRecentFiles()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = if (context is IMainActivity) {
            context
        } else {
            throw RuntimeException(
                DocsRecentFragment::class.java.simpleName + " - must implement - " +
                        MainActivity::class.java.simpleName
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (isResumed) {
            clearItem = menu.findItem(R.id.toolbar_item_empty_trash)
            clearItem?.isVisible = true
            clearItem?.let { item ->
                UiUtils.setMenuItemTint(requireContext(), item, lib.toolkit.base.R.color.colorPrimary)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_item_empty_trash -> {
                showClearDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey(KEY_FILTER) == true) {
            filterValue = savedInstanceState.getCharSequence(KEY_FILTER)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(KEY_FILTER, searchView?.query)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.setSectionType(ApiContract.SectionType.CLOUD_RECENT)
        init()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        setMenuSearchEnabled(true)
        mainItem?.isVisible = true
        clearItem?.let { item ->
            UiUtils.setMenuItemTint(requireContext(), item, lib.toolkit.base.R.color.colorPrimary)
        }
    }

    override fun onStateUpdateFilter(isFilter: Boolean, value: String?) {
        super.onStateUpdateFilter(isFilter, value)
        if (isFilter) {
            activity?.setAppBarStates(false)
            searchView?.setQuery(filterValue, true)
            filterValue = ""
        } else {
            activity?.setAppBarStates(false)
            activity?.showNavigationButton(false)
        }
    }

    override fun onStateEmptyBackStack() {
        // stub
    }

    private fun init() {
        activity?.let { activity ->
            activity.setAppBarStates(false)
            activity.showNavigationButton(false)
            activity.showActionButton(false)
            activity.showAccount(false)
        }
        adapter = RecentAdapter(requireContext(), RecentHolderFactory(recentListener, contextListener))
        recyclerView?.let {
            it.adapter = adapter
            it.setPadding(
                resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_left_right_padding),
                resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_top_bottom_padding),
                resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_left_right_padding),
                resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_bottom_padding)
            )
        }
        swipeRefreshLayout?.isEnabled = false
        checkStorage()
        setActionBarTitle(getString(R.string.fragment_recent_title))
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        requestManage()
    }

    private fun checkStorage() {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> {
                requestReadWritePermission()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                requestAccessStorage()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestAccessStorage() {
        if (!Environment.isExternalStorageManager() && requireContext().appComponent.preference.isShowStorageAccess) {
            showQuestionDialog(
                getString(R.string.app_manage_files_title),
                getString(R.string.app_manage_files_description),
                getString(R.string.dialogs_common_ok_button),
                getString(R.string.dialogs_common_cancel_button),
                TAG_STORAGE_ACCESS
            )
        } else {
            presenter.getRecentFiles()
        }
    }

    private fun requestReadWritePermission() {
        RequestPermissions(requireActivity().activityResultRegistry, { permissions ->
            if (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                presenter.recreateStack()
                presenter.getRecentFiles()
            } else {
                swipeRefreshLayout?.isEnabled = false
                placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.ACCESS)
            }
        }, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)).request()
    }

    override fun updateFiles(files: List<Recent>, sortBy: String, sortOrder: String) {
        if (files.isNotEmpty()) {
            adapter?.setRecent(files, sortBy == ApiContract.Parameters.VAL_SORT_BY_UPDATED)
            recyclerView?.scrollToPosition(0)
            placeholderViews?.setVisibility(false)
            updateMenu(true)
        } else {
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.SEARCH)
            updateMenu(false)
        }
    }

    private fun updateMenu(isEnable: Boolean) {
        if (menu != null && searchItem != null && deleteItem != null) {
            searchItem?.isEnabled = isEnable
            deleteItem?.isVisible = isEnable
        }
    }

    override fun openFile(response: CloudFile) {
        val ext = response.fileExst
        if (StringUtils.isVideoSupport(ext) || StringUtils.isImage(ext)) {
            showMediaActivity(getExplorer(response), false) {
                // Stub
            }
        } else if (StringUtils.isDocument(ext)) {
            activity?.showWebViewer(response)
        } else {
            onError(getString(R.string.error_unsupported_format))
        }
    }

    private fun getExplorer(file: CloudFile): Explorer {
        return Explorer().apply {
            this.files = mutableListOf(file)
        }
    }

    override fun onQueryTextChange(newText: String): Boolean {
        searchCloseButton?.isEnabled = newText.isNotEmpty()
        presenter.searchRecent(newText)
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onContextShow(state: ContextBottomDialog.State) {
        parentFragmentManager.let {
//            contextBottomDialog?.state = state
            contextBottomDialog?.onClickListener = this
            contextBottomDialog?.show(it, ContextBottomDialog.TAG)
        }
    }

    override fun onDeleteItem(position: Int) {
        adapter?.let { recentAdapter ->
            recentAdapter.removeItem(position)
            if (recentAdapter.isEmpty()) setEmpty()
        }
    }

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        if (buttons == ContextBottomDialog.Buttons.DELETE) {
            presenter.deleteRecent()
        }
        contextBottomDialog?.dismiss()
    }

    override fun onRender(state: RecentState) {
        when (state) {
            is RecentState.RenderList -> {
                if (state.recents.isEmpty()) {
                    setEmpty()
                } else {
                    setRecent(state.recents)
                }
            }
        }
    }

    private fun setRecent(recent: List<Recent>) {
        setMenuVisibility(true)
        presenter.updateFiles(recent)
    }

    private fun setEmpty() {
        setMenuVisibility(false)
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.EMPTY)
    }

    override fun onOpenFile(state: OpenState) {
        when (state) {
            is OpenState.Docs, is OpenState.Cells, is OpenState.Slide, is OpenState.Pdf -> {
                LaunchActivityForResult(
                    requireActivity().activityResultRegistry,
                    { result ->
                        if (result.resultCode == Activity.RESULT_CANCELED) {
                            presenter.deleteTempFile()
                        } else if (result.resultCode == Activity.RESULT_OK) {
                            result.data?.data?.let {
                                if (result.data?.getBooleanExtra("EXTRA_IS_MODIFIED", false) == true) {
                                    presenter.upload(it, null)
                                }
                            }
                        }
                    },
                    getEditorsIntent(state.uri, checkNotNull(state.type))
                ).show()
            }
            is OpenState.Media -> {
                showMediaActivity(state.explorer, state.isWebDav) {
                    // Stub
                }
            }
        }
    }

    override fun showMainActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showMainActionBarMenu(
            listOf(
                MainActionBarPopup.Author,
                MainActionBarPopup.SelectAll,
                MainActionBarPopup.Select
            )
        )
    }

    override val isWebDav: Boolean
        get() = false

    object Debounce {
        private var isClickable = true

        fun perform(timeMillis: Long, func: () -> Unit) {
            if (isClickable) {
                CoroutineScope(Dispatchers.Main).launch {
                    func.invoke()
                    isClickable = false
                    delay(timeMillis)
                    isClickable = true
                }
            }
        }
    }

    private fun showClearDialog() {
        UiUtils.showQuestionDialog(requireContext(), "Clear recents?", acceptListener = {
            presenter.clearRecents()
        }, cancelListener = {
            // stub
        })
    }

    private fun requestManage() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                readStorage.launch(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:" + requireContext().packageName)
                    )
                )
            }
        } catch (e: ActivityNotFoundException) {
            openItem?.isVisible = false
            swipeRefreshLayout?.isEnabled = false
            activity?.showActionButton(false)
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.ACCESS)
        }
    }

    override val mainActionBarClickListener: (ActionBarPopupItem) -> Unit = { item ->
        if (item == MainActionBarPopup.getSortPopupItem(presenter.preferenceTool.sortBy)) {
            presenter.reverseOrder()
        } else {
            when (item) {
                MainActionBarPopup.Date ->
                    presenter.update(ApiContract.Parameters.VAL_SORT_BY_UPDATED)
                MainActionBarPopup.Type ->
                    presenter.update(ApiContract.Parameters.VAL_SORT_BY_TYPE)
                MainActionBarPopup.Size ->
                    presenter.update(ApiContract.Parameters.VAL_SORT_BY_SIZE)
                MainActionBarPopup.Title ->
                    presenter.update(ApiContract.Parameters.VAL_SORT_BY_TITLE)
            }
        }
    }

    companion object {
        var TAG: String = DocsRecentFragment::class.java.simpleName

        private const val TAG_STORAGE_ACCESS = "TAG_STORAGE_ACCESS"

        fun newInstance(): DocsRecentFragment {
            return DocsRecentFragment()
        }

        private const val KEY_FILTER = "KEY_FILTER"
    }

}