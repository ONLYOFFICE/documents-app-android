package app.editors.manager.ui.fragments.filter

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import app.editors.manager.R
import app.editors.manager.mvp.models.filter.Author
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.presenters.filter.FilterAuthorPresenter
import app.editors.manager.mvp.views.filter.FilterAuthorView
import app.editors.manager.ui.activities.main.FilterActivity
import app.editors.manager.ui.activities.main.IFilterActivity
import app.editors.manager.ui.adapters.AuthorAdapter
import app.editors.manager.ui.adapters.diffutilscallback.AuthorDiffUtilsCallback
import app.editors.manager.ui.dialogs.fragments.IBaseDialogFragment
import app.editors.manager.ui.fragments.base.ListFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import lib.toolkit.base.ui.views.search.CommonSearchView
import moxy.presenter.InjectPresenter

class FilterAuthorFragment : ListFragment(), FilterAuthorView {

    companion object {
        private const val KEY_AUTHOR_ID = "key_author_id"
        private const val KEY_IS_GROUPS = "key_is_groups"
        private const val KEY_IS_ROOM = "key_is_room"
        const val REQUEST_KEY_AUTHOR = "request_key_author"
        const val BUNDLE_KEY_AUTHOR = "bundle_key_author"

        val TAG = FilterAuthorFragment::class.simpleName

        fun newInstance(authorId: String, isGroups: Boolean = false, isRoom: Boolean = false): FilterAuthorFragment {
            return FilterAuthorFragment().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_AUTHOR_ID, authorId)
                    putBoolean(KEY_IS_GROUPS, isGroups)
                    putBoolean(KEY_IS_ROOM, isRoom)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: FilterAuthorPresenter

    private var authorAdapter: AuthorAdapter? = null
    private var searchView: CommonSearchView? = null
    private var searchItem: MenuItem? = null
    private var activity: IFilterActivity? = null
    private val dialog: IBaseDialogFragment? get() = getDialogFragment()

    private val isGroups: Boolean
        get() = arguments?.getBoolean(KEY_IS_GROUPS) == true

    private val clickListener: (FilterAuthor) -> Unit = { author ->
        setFragmentResult(REQUEST_KEY_AUTHOR, Bundle(1).apply {
            putString(BUNDLE_KEY_AUTHOR, FilterAuthor.toJson(author))
        })
        parentFragmentManager.popBackStack()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!isTablet) {
            activity = try {
                context as IFilterActivity
            } catch (e: ClassCastException) {
                throw RuntimeException(
                    FilterActivity::class.java.simpleName + " - must implement - " +
                            IFilterActivity::class.java.simpleName
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        searchItem = if (isTablet) {
            dialog?.getMenu()?.findItem(R.id.toolbar_item_search)
        } else {
            menu.findItem(R.id.toolbar_item_search)
        }
        initSearchView(checkNotNull(searchItem?.actionView as? SearchView))
    }

    override fun onBackPressed(): Boolean {
        return when {
            presenter.isSearchingMode -> {
                presenter.isSearchingMode = false
                searchView?.collapse()
                true
            }
            else -> false
        }
    }

    override fun onRefresh() {}

    override fun onGetUsers(users: List<Author.User>) {
        searchItem?.isVisible = users.isNotEmpty()
        if (users.isEmpty()) {
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.USERS)
        } else {
            authorAdapter?.itemsList = users
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
        }
    }

    override fun onGetGroups(groups: List<Author.Group>) {
        searchItem?.isVisible = groups.isNotEmpty()
        if (groups.isEmpty()) {
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.GROUPS)
        } else {
            authorAdapter?.itemsList = groups
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
        }
    }

    override fun onLoadingGroups() {
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.LOAD_GROUPS)
    }

    override fun onLoadingUsers() {
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.LOAD_USERS)
    }

    override fun onUpdateAvatar(user: Author.User) {
        authorAdapter?.let { adapter ->
            val position = adapter.updateItem(user)
            adapter.notifyItemChanged(position, AuthorAdapter.PAYLOAD_AVATAR)
        }
    }

    override fun onSearchResult(authors: List<Author>) {
        searchItem?.isVisible = true
        authorAdapter?.itemsList?.let { oldList ->
            if (authors.isNotEmpty()) {
                placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
                val diffUtils = AuthorDiffUtilsCallback(authors, oldList)
                val result = DiffUtil.calculateDiff(diffUtils)
                authorAdapter?.set(authors, result)
                recyclerView?.smoothScrollToPosition(0)
            } else {
                if (isGroups) {
                    placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.GROUPS)
                } else {
                    placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.USERS)
                }
            }
        }
    }

    override fun onError(message: String?) {
        message?.let(::showToast)
    }

    override fun onUnauthorized(message: String?) {
        message?.let(::showToast)
    }

    private fun init() {
        swipeRefreshLayout?.isEnabled = false
        authorAdapter = AuthorAdapter(arguments?.getString(KEY_AUTHOR_ID), clickListener)
        recyclerView?.adapter = authorAdapter

        initToolbar()
        getAuthorList()
        setHasOptionsMenu(true)
    }

    private fun initToolbar() {
        val toolbarTitle = if (!isGroups) getString(R.string.filter_toolbar_users_title)
        else getString(R.string.filter_toolbar_groups_title)
        if (isTablet) {
            dialog?.setToolbarButtonVisible(isVisible = false)
            dialog?.setToolbarNavigationIcon(isClose = false)
            dialog?.setToolbarTitle(title = toolbarTitle)
        } else {
            activity?.setResetButtonVisible(isVisible = false)
            setActionBarTitle(title = toolbarTitle)
        }
    }

    private fun initSearchView(actionView: SearchView) {
        searchView = CommonSearchView(
            coroutineScope = lifecycleScope,
            searchView = actionView,
            isExpanded = presenter.isSearchingMode,
            onQuery = presenter::search,
            onExpand = { presenter.isSearchingMode = true },
        ).also { if (presenter.isSearchingMode) it.query = presenter.searchingValue }
    }

    private fun getAuthorList() {
        if (!presenter.isSearchingMode) {
            if (isGroups) {
                presenter.getGroups()
            } else {
                presenter.getUsers(arguments?.getBoolean(KEY_IS_ROOM) != true)
            }
        }
    }
}
