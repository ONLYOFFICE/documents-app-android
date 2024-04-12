package app.editors.manager.mvp.presenters.storage

import app.documents.core.network.manager.models.request.RequestStorage
import app.documents.core.network.manager.models.response.ResponseFolder
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.storage.ConnectView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState

@InjectViewState
class ConnectPresenter : BasePresenter<ConnectView>() {

    companion object {
        val TAG: String = ConnectPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable = null
    }

    fun connectService(token: String?, providerKey: String?, title: String?, isCorporate: Boolean) {
        connectStorage(
            RequestStorage(
                token = token,
                providerKey = providerKey,
                customerTitle = title,
                corporate = isCorporate
            )
        )
    }

    fun connectWebDav(
        providerKey: String?,
        url: String?,
        login: String?,
        password: String?,
        title: String?,
        isCorporate: Boolean
    ) {
        connectStorage(
            RequestStorage(
                providerKey = providerKey,
                url = url,
                login = login,
                password = password,
                customerTitle = title,
                corporate = isCorporate
            )
        )
    }

    private fun connectStorage(requestStorage: RequestStorage) {
        disposable = context.api.connectStorage(requestStorage)
            .subscribeOn(Schedulers.io())
            .map(ResponseFolder::response)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(viewState::onConnect, ::fetchError)
    }

}