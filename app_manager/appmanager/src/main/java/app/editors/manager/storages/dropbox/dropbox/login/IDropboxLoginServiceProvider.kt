package app.editors.manager.storages.dropbox.dropbox.login

import app.editors.manager.storages.dropbox.mvp.models.request.AccountRequest
import app.editors.manager.storages.dropbox.mvp.models.response.TokenResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response


sealed class DropboxResponse {
    class Success(val response: Any) : DropboxResponse()
    class Error(val error: Throwable) : DropboxResponse()
}

interface IDropboxLoginServiceProvider {
    fun getRefreshToken(auth: String, map: Map<String, String>): Single<DropboxResponse>
    fun updateRefreshToken(auth: String, map: Map<String, String>): Single<DropboxResponse>
    fun getUserInfo(token: String, request: AccountRequest): Single<DropboxResponse>
}