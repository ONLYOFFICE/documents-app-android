package app.editors.manager.ui.fragments.filter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import app.editors.manager.R
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.presenters.filter.FilterAuthorPresenter
import app.editors.manager.mvp.views.filter.FilterAuthorView
import app.editors.manager.ui.adapters.AuthorAdapter
import app.editors.manager.ui.fragments.base.ListFragment
import moxy.presenter.InjectPresenter

sealed class Author {
    class Group(val id: String, val title: String) : Author()
    class User(val id: String, val name: String, val department: String, val avatarUrl: String) : Author()
}

class FilterAuthorFragment : ListFragment(), FilterAuthorView {

    companion object {
        private const val KEY_AUTHOR_ID = "key_author_id"
        private const val KEY_IS_GROUPS = "key_is_groups"
        const val REQUEST_KEY_AUTHOR = "request_key_author"
        const val BUNDLE_KEY_AUTHOR = "bundle_key_author"

        val TAG = FilterAuthorFragment::class.simpleName

        fun newInstance(authorId: String, isGroups: Boolean): FilterAuthorFragment {
            return FilterAuthorFragment().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_AUTHOR_ID, authorId)
                    putBoolean(KEY_IS_GROUPS, isGroups)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: FilterAuthorPresenter

    private var authorAdapter: AuthorAdapter? = null

    private val isGroups: Boolean
        get() = arguments?.getBoolean(KEY_IS_GROUPS) == true

    private val clickListener: (FilterAuthor) -> Unit = { author ->
        setFragmentResult(REQUEST_KEY_AUTHOR, Bundle(1).apply {
            putString(BUNDLE_KEY_AUTHOR, FilterAuthor.toJson(author))
        })
        parentFragmentManager.popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onRefresh() { }

    override fun onGetUsers(users: List<Author.User>) {
        authorAdapter?.setItems(users)
    }

    override fun onGetGroups(groups: List<Author.Group>) {
        authorAdapter?.setItems(groups)
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
        getAuthorList()
        setActionBarTitle(
            if (!isGroups) getString(R.string.filter_toolbar_users_title)
            else getString(R.string.filter_toolbar_groups_title)
        )
    }

    private fun getAuthorList() {
        if (isGroups) {
            presenter.getGroups()
        } else {
            presenter.getUsers()
        }
    }
}
