package app.editors.manager.mvp.presenters.filter

import app.documents.core.network.common.extensions.request
import app.documents.core.network.share.ShareService
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.shareApi
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.mvp.models.filter.Author
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.filter.FilterAuthorView
import kotlinx.coroutines.*
import moxy.presenterScope

class FilterAuthorPresenter : BasePresenter<FilterAuthorView>() {

    companion object {
        private const val DEBOUNCE_DURATION = 300L
    }

    private val authorStack: MutableList<Author> = mutableListOf()
    private var job: Job? = null
    var searchingValue: String = ""
    var isSearchingMode: Boolean = false

    private val shareApi: ShareService
        get() = context.shareApi

    private val accountId: String?
        get() = context.accountOnline?.id

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        job = null
    }

    private fun loadAvatars(users: List<Author.User>) {
        presenterScope.launch {
            users.request(
                func = { user -> GlideUtils.getAvatarFromUrl(context, user.avatarUrl) },
                map = { user, avatar -> user.also { it.avatar = avatar } },
                onEach = viewState::onUpdateAvatar
            )
        }
    }

    private fun setStackItems(items: List<Author>) {
        authorStack.clear()
        authorStack.addAll(items)
    }

    private fun List<Author.User>.moveOwnerToFirstPosition(withSelf: Boolean): List<Author.User> {
        return apply {
            find { it.id == accountId }?.let { user ->
                (this as MutableList).apply {
                    remove(user)
                    if (withSelf) {
                        add(
                            index = 0,
                            element = Author.User(
                                id = user.id,
                                name = context.getString(R.string.item_owner_self),
                                department = user.department,
                                avatarUrl = user.avatarUrl
                            )
                        )
                    }
                }
            }
        }
    }

    fun getUsers(withSelf: Boolean = true) {
        presenterScope.launch {
            request(
                func = shareApi::getUsers,
                map = { response ->
                    response.response
                        .map { Author.User(it.id, it.displayName, it.department, it.avatar) }
                        .moveOwnerToFirstPosition(withSelf)
                        .also(this@FilterAuthorPresenter::setStackItems)
                },
                onSuccess = { users ->
                    viewState.onGetUsers(users)
                    loadAvatars(users)
                },
                onError = ::fetchError
            )
        }
    }

    fun getGroups() {
        presenterScope.launch {
            request(
                func = shareApi::getGroups,
                map = { response ->
                    response.response.map { Author.Group(it.id, it.name) }
                        .also(this@FilterAuthorPresenter::setStackItems)

                },
                onSuccess = viewState::onGetGroups,
                onError = ::fetchError
            )
        }
    }

    fun search(value: String) {
        searchingValue = value
        job?.cancel()
        job = presenterScope.launch {
            delay(DEBOUNCE_DURATION)
            val authors = authorStack.filter { it.name.contains(searchingValue, true) }
            withContext(Dispatchers.Main) {
                viewState.onSearchResult(authors)
            }
        }
    }
}