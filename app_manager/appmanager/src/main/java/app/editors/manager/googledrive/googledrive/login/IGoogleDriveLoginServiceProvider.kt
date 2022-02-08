package app.editors.manager.googledrive.googledrive.login

import app.editors.manager.dropbox.mvp.models.response.UserResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response


sealed class GoogleDriveResponse {
    class Success(val response: Any) : GoogleDriveResponse()
    class Error(val error: Throwable) : GoogleDriveResponse()
}

interface IGoogleDriveLoginServiceProvider {

    fun getToken(map: Map<String, String>): Single<Response<ResponseBody>>
    fun getUserInfo(token: String): Single<GoogleDriveResponse>
}