package app.editors.manager.storages.dropbox.dropbox.login

import app.editors.manager.storages.dropbox.mvp.models.request.AccountRequest
import io.reactivex.Single


sealed class DropboxResponse {
    class Success(val response: Any) : DropboxResponse()
    class Error(val error: Throwable) : DropboxResponse()
}

interface IDropboxLoginServiceProvider {
    fun getUserInfo(token: String, request: AccountRequest): Single<DropboxResponse>
}