package app.editors.manager.mvp.presenters.filter

import app.documents.core.share.ShareService
import app.editors.manager.app.App
import app.editors.manager.app.getShareApi
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.filter.FilterAuthorView
import app.editors.manager.ui.fragments.filter.Author
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class FilterAuthorPresenter : BasePresenter<FilterAuthorView>() {

    private var disposable: Disposable? = null

    private val api: ShareService
        get() = App.getApp().appComponent.context.getShareApi()

    fun getUsers() {
        disposable = api.getUsers()
            .subscribeOn(Schedulers.io())
            .map { response ->
                response.response.map {
                    Author.User(it.id, it.displayName, it.department, it.avatarMedium)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(viewState::onGetUsers, this::fetchError)
    }

    fun getGroups() {
        disposable = api.getGroups()
            .subscribeOn(Schedulers.io())
            .map { response ->
                response.response.map {
                    Author.Group(it.id, it.name)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(viewState::onGetGroups, this::fetchError)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable = null
    }
}