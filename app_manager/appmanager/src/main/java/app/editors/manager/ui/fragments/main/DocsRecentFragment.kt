package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import app.editors.manager.ui.adapters.holders.factory.RecentHolderFactory
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.popup.MainActionBarPopup
import app.editors.manager.ui.views.custom.PlaceholderViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.popup.ActionBarPopupItem
import moxy.presenter.InjectPresenter

class DocsRecentFragment : DocsBaseFragment(), DocsRecentView {

    @InjectPresenter
    override lateinit var presenter: DocsRecentPresenter

    private var activity: IMainActivity? = null
    private var adapter: RecentAdapter? = null
    private var filterValue: CharSequence? = null

    private val recentListener: (recent: Recent, position: Int) -> Unit = { recent, position ->
        Debounce.perform(1000L) { presenter.fileClick(recent, position) }
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
        mainItem?.isVisible = true
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
            filterValue = ""
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
        if (checkReadPermission()) {
            presenter.getRecentFiles()
        }
        setActionBarTitle(getString(R.string.fragment_recent_title))
    }

    override fun onListEnd() {
        presenter.loadMore(adapter?.itemCount)
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
        if (menu != null && sortItem != null && searchItem != null && deleteItem != null) {
            sortItem?.isEnabled = isEnable
            searchItem?.isEnabled = isEnable
            deleteItem?.isVisible = isEnable
        }
    }

    override fun openFile(response: CloudFile) {
        val ext = response.fileExst
        if (StringUtils.isVideoSupport(ext) || StringUtils.isImage(ext)) {
            MediaActivity.show(this, getExplorer(response), false)
        } else if (StringUtils.isDocument(ext)) {
            WebViewerActivity.show(requireActivity(), response)
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
            contextBottomDialog?.state = state
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

    override fun onUpdateItemFavorites() { }

    override fun showMainActionBarMenu(itemId: Int, excluded: List<ActionBarPopupItem>) {
        super.showMainActionBarMenu(
            itemId = itemId,
            excluded = listOf(
                MainActionBarPopup.Author,
                MainActionBarPopup.SelectAll,
                MainActionBarPopup.Select
            )
        )
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

        fun newInstance(): DocsRecentFragment {
            return DocsRecentFragment()
        }

        private const val KEY_FILTER = "KEY_FILTER"
    }

}