package app.editors.manager.dropbox.mvp.presenters

import app.editors.manager.app.App
import app.editors.manager.app.dropboxLoginService
import app.editors.manager.dropbox.mvp.models.AccountRequest
import app.editors.manager.dropbox.mvp.views.DropboxSignInView
import app.editors.manager.mvp.presenters.base.BasePresenter
import io.reactivex.disposables.Disposable

class DropboxSignInPresenter: BasePresenter<DropboxSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getUserInfo(token: String, uid: String) {
        val accountRequest = AccountRequest(account_id = uid)
        val map = mapOf("account_id" to uid)
        disposable = App.getApp().dropboxLoginService.getUserInfo("Bearer $token", map)
            .subscribe { result ->
                result.body()?.string()
            }
    }

}