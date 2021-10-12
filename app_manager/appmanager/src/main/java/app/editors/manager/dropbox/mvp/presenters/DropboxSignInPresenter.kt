package app.editors.manager.dropbox.mvp.presenters

import app.editors.manager.app.App
import app.editors.manager.app.dropboxLoginService
import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.mvp.models.request.AccountRequest
import app.editors.manager.dropbox.mvp.models.response.UserResponse
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
        val accountRequest = AccountRequest(account_id = uid.replace("%3A", ":"))
        disposable = App.getApp().dropboxLoginService.getUserInfo("Bearer $token", accountRequest)
            .subscribe { response ->
                when(response) {
                    is DropboxResponse.Success -> {
                        val user = response.response as UserResponse
                    }
                    is DropboxResponse.Error -> {
                        throw response.error
                    }
                }
            }
    }

}