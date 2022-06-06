package app.editors.manager.mvp.presenters.filter

import app.documents.core.share.ShareService
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.getShareApi
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.mvp.models.filter.Author
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.filter.FilterAuthorView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*

class FilterAuthorPresenter : BasePresenter<FilterAuthorView>() {

    companion object {
        private const val DEBOUNCE_DURATION = 300L
    }

    private var disposable: Disposable? = null
    private val authorStack: MutableList<Author> = mutableListOf()
    private var job: Job? = null
    var searchingValue: String = ""
    var isSearchingMode: Boolean = false

    private val api: ShareService
        get() = App.getApp().appComponent.context.getShareApi()

    private val accountId: String?
        get() = App.getApp().appComponent.context.accountOnline?.id

    private val avatarMapper: (Author.User) -> Author.User = { user ->
        user.also {
            user.avatar = GlideUtils.loadAvatar(user.avatarUrl)
        }
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable = null
        job = null
    }

    private fun loadAvatars(list: List<Author.User>) {
        disposable = Observable.fromIterable(list)
            .subscribeOn(Schedulers.io())
            .map(avatarMapper)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(viewState::onUpdateAvatar, this::fetchError)
    }

    private fun setStackItems(items: List<Author>) {
        authorStack.clear()
        authorStack.addAll(items)
    }

    private fun List<Author.User>.moveOwnerToFirstPosition(): List<Author.User> {
        return apply {
            find { it.id == accountId }?.let { user ->
                (this as MutableList).apply {
                    remove(user)
                    add(
                        index = 0,
                        element = Author.User(
                            id = user.id,
                            name = context.getString(R.string.filter_author_owner),
                            department = user.department,
                            avatarUrl = user.avatarUrl
                        )
                    )
                }
            }
        }
    }

    fun getUsers() {
        disposable = api.getUsers()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { viewState.onLoadingUsers() }
            .map { response ->
                response.response
                    .map { Author.User(it.id, it.displayName, it.department, it.avatarMedium) }
                    .moveOwnerToFirstPosition()
                    .also(this::setStackItems)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ users ->
                viewState.onGetUsers(users)
                loadAvatars(users)
            }, this::fetchError)
    }

    fun getGroups() {
        disposable = api.getGroups()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { viewState.onLoadingGroups() }
            .map { response ->
                response.response.map { Author.Group(it.id, it.name) }.also(this::setStackItems)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(viewState::onGetGroups, this::fetchError)
    }

    fun search(value: String) {
        searchingValue = value
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            delay(DEBOUNCE_DURATION)
            val authors = authorStack.filter { it.name.contains(searchingValue, true) }
            withContext(Dispatchers.Main) {
                viewState.onSearchResult(authors)
            }
        }
    }
}