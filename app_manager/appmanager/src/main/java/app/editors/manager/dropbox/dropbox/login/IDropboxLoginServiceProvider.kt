package app.editors.manager.dropbox.dropbox.login

import app.editors.manager.dropbox.mvp.models.AccountRequest
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response


sealed class DropboxResponse {
    class Success(val response: Any) : DropboxResponse()
    class Error(val error: Throwable) : DropboxResponse()
}

interface IDropboxLoginServiceProvider {
    fun getUserInfo(token: String, request: Map<String, String>): Single<Response<ResponseBody>>
}