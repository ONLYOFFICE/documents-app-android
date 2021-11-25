package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import app.documents.core.account.Recent
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.presenters.main.DocsRecentPresenter
import app.editors.manager.mvp.presenters.main.OpenState
import app.editors.manager.mvp.presenters.main.RecentState
import app.editors.manager.mvp.views.main.DocsRecentView
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.activities.main.MediaActivity
import app.editors.manager.ui.activities.main.WebViewerActivity
import app.editors.manager.ui.adapters.RecentAdapter
import app.editors.manager.ui.adapters.diffutilscallback.RecentDiffUtilsCallback
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

class DocsRecentFragment : DocsBaseFragment(), DocsRecentView {

    @InjectPresenter
    override lateinit var presenter: DocsRecentPresenter

    private var activity: IMainActivity? = null
    private var adapter: RecentAdapter? = null
    private var filterValue: CharSequence? = null

    private val recentListener: (recent: Recent, position: Int) -> Unit = { recent, position ->
            Debounce.perform(1000L) {
                presenter.fileClick(recent, position)
            }
    }

    private val contextListener: (recent: Recent, position: Int) -> Unit = { recent, position ->
        presenter.contextClick(recent, position)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_READ_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.getRecentFiles()
            } else {
                placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.ACCESS)
            }
        }
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
        init()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        setMenuSearchEnabled(true)
        mainItem?.isVisible = false
        sortItem?.let {
            it.isVisible = true
            it.isEnabled = true
            it.subMenu.findItem(R.id.toolbar_sort_item_owner).isVisible = false
            //mSortItem.getSubMenu().findItem(R.id.toolbar_sort_item_date_update).setChecked(true);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            BaseActivity.REQUEST_ACTIVITY_WEB_VIEWER -> presenter.getRecentFiles()
            REQUEST_DOCS, REQUEST_SHEETS, REQUEST_PRESENTATION, REQUEST_PDF ->
                if (resultCode == Activity.RESULT_CANCELED) {
                    presenter.deleteTempFile()
                } else if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        if (data.getBooleanExtra("EXTRA_IS_MODIFIED", false)) {
                            presenter.upload(it, null)
                        }
                    }
                }
        }
    }

    override fun onStateUpdateFilter(isFilter: Boolean, value: String?) {
        super.onStateUpdateFilter(isFilter, value)
        if (isFilter) {
            activity?.setAppBarStates(false)
            searchView?.setQuery(filterValue, true)
        } else {
            activity?.setAppBarStates(false)
            activity?.showNavigationButton(false)
        }
    }

    private fun init() {
        activity?.let { activity ->
            activity.setAppBarStates(false)
            activity.showNavigationButton(false)
            activity.showActionButton(false)
            activity.showAccount(false)
        }
        adapter = RecentAdapter(requireContext(), recentListener, contextListener)
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
        if (checkReadPermission()) {
            presenter.getRecentFiles()
        }
        setActionBarTitle(getString(R.string.fragment_recent_title))
    }

    override fun onRecentGet(list: List<Recent>) {
        adapter?.setItems(list)
    }

    override fun onListEnd() {
        presenter.loadMore(adapter?.itemCount)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        menu?.let { menu ->
            val isAscending = menu.findItem(R.id.toolbar_sort_item_asc).isChecked
            when (item.itemId) {
                R.id.toolbar_item_sort -> {
                    activity?.setAppBarStates(false)
                    activity?.showNavigationButton(false)
                }
                R.id.toolbar_sort_item_title -> {
                    if (item.isChecked) {
                        presenter.reverseSortOrder(adapter!!.itemList)
                    } else {
                        presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_TITLE, isAscending)
                    }
                    item.isChecked = true
                }
                R.id.toolbar_sort_item_date_update -> {
                    if (item.isChecked) {
                        presenter.reverseSortOrder(adapter!!.itemList)
                    } else {
                        presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_UPDATED, isAscending)
                    }
                    item.isChecked = true
                }
                R.id.toolbar_sort_item_owner -> {
                    if (item.isChecked) {
                        presenter.reverseSortOrder(adapter!!.itemList)
                    } else {
                        presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_OWNER, isAscending)
                    }
                    item.isChecked = true
                }
                R.id.toolbar_sort_item_size -> {
                    if (item.isChecked) {
                        presenter.reverseSortOrder(adapter!!.itemList)
                    } else {
                        presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_SIZE, isAscending)
                    }
                    item.isChecked = true
                }
                R.id.toolbar_sort_item_type -> {
                    if (item.isChecked) {
                        presenter.reverseSortOrder(adapter!!.itemList)
                    } else {
                        presenter.sortBy(ApiContract.Parameters.VAL_SORT_BY_TYPE, isAscending)
                    }
                    item.isChecked = true
                }
                R.id.toolbar_sort_item_asc, R.id.toolbar_sort_item_desc -> presenter.reverseList(
                    adapter!!.itemList,
                    isAscending
                )
                else -> {}
            }
        }
        item.isChecked = true
        return false
    }

    override fun onReverseSortOrder(itemList: List<Recent>) {
        adapter?.setData(itemList)
        adapter?.notifyDataSetChanged()
        menu?.let { menu ->
            if (menu.findItem(R.id.toolbar_sort_item_desc)?.isChecked == true) {
                menu.findItem(R.id.toolbar_sort_item_asc)?.isChecked = true
            } else {
                menu.findItem(R.id.toolbar_sort_item_desc)?.isChecked = true
            }
        }
    }

    override fun updateFiles(files: List<Recent>) {
        if (files.isNotEmpty()) {
            adapter?.itemList?.let {
                updateDiffUtils(files)
                recyclerView?.scrollToPosition(0)
            } ?: run {
                adapter?.setItems(files)
            }
            placeholderViews?.setVisibility(false)
            updateMenu(true)
        } else {
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.SEARCH)
            updateMenu(false)
        }
    }

    private fun updateMenu(isEnable: Boolean) {
        if (menu != null && sortItem != null && searchItem != null && deleteItem != null) {
            sortItem?.isEnabled = isEnable
            searchItem?.isEnabled = isEnable
            deleteItem?.isVisible = isEnable
        }
    }

    private fun updateDiffUtils(files: List<Recent>) {
        val diffUtils = RecentDiffUtilsCallback(files, adapter?.itemList)
        val result = DiffUtil.calculateDiff(diffUtils)
        adapter?.set(files, result)
    }

    override fun openFile(file: CloudFile) {
        val ext = file.fileExst
        if (StringUtils.isVideoSupport(ext) || StringUtils.isImage(ext)) {
            MediaActivity.show(this, getExplorer(file), false)
        } else if (StringUtils.isDocument(ext)) {
            WebViewerActivity.show(requireActivity(), file)
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

    override fun onMoveElement(recent: Recent, position: Int) {
        adapter?.moveItem(position, 0)
        recyclerView?.scrollToPosition(0)
    }

    override fun onContextShow(state: ContextBottomDialog.State) {
        parentFragmentManager.let {
            contextBottomDialog?.state = state
            contextBottomDialog?.onClickListener = this
            contextBottomDialog?.show(it, ContextBottomDialog.TAG)
        }
    }

    override fun onDeleteItem(position: Int) {
        adapter?.let { recentAdapter ->
            recentAdapter.removeItem(position)
            if (recentAdapter.itemCount == 0) setEmpty()
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
                    setRecents(state.recents)
                }
            }
        }
    }

    private fun setRecents(recents: List<Recent>) {
        setMenuVisibility(true)
        adapter?.setItems(recents)
    }

    private fun setEmpty() {
        setMenuVisibility(false)
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.EMPTY)
    }

    override fun onOpenFile(state: OpenState) {
        when (state) {
            is OpenState.Docs -> {
                showEditors(state.uri, EditorsType.DOCS)
            }
            is OpenState.Cells -> {
                showEditors(state.uri, EditorsType.CELLS)
            }
            is OpenState.Slide -> {
                showEditors(state.uri, EditorsType.PRESENTATION)
            }
            is OpenState.Pdf -> {
                showEditors(state.uri, EditorsType.PDF)
            }
            is OpenState.Media -> {
                MediaActivity.show(this, state.explorer, state.isWebDav)
            }
        }
    }

    override fun onRemoveItemFromFavorites() {

    }

    override val isWebDav: Boolean
        get() = false

    object Debounce {
        var isClickable = true

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

    companion object {
        var TAG: String = DocsRecentFragment::class.java.simpleName

        fun newInstance(): DocsRecentFragment {
            return DocsRecentFragment()
        }

        private const val KEY_FILTER = "KEY_FILTER"
    }

}