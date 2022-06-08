package app.editors.manager.storages.dropbox.dropbox.login

import app.editors.manager.storages.dropbox.mvp.models.request.AccountRequest
import app.editors.manager.storages.dropbox.mvp.models.response.TokenResponse
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.QueryMap

class DropboxLoginServiceProvider(
    private val dropBoxLoginService: DropboxLoginService,
    private val dropboxErrorHandler: BehaviorRelay<DropboxResponse.Error>? = null
): IDropboxLoginServiceProvider {

    override fun getUserInfo(token: String, request: AccountRequest): Single<DropboxResponse> {
        return dropBoxLoginService.getUserInfo(token, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRefreshToken(auth: String, map: Map<String, String>): Single<DropboxResponse> {
        return dropBoxLoginService.getRefreshToken(auth, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun updateRefreshToken(auth: String, map: Map<String, String>): Single<DropboxResponse> {
        return dropBoxLoginService.updateRefreshToken(auth, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> fetchResponse(response: Response<T>): DropboxResponse {
        return if (response.isSuccessful && response.body() != null) {
            DropboxResponse.Success(response.body()!!)
        } else {
            val error = DropboxResponse.Error(HttpException(response))
            dropboxErrorHandler?.accept(error)
            return error
        }
    }
}