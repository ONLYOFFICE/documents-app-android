package app.documents.core.network.storages.dropbox.login

import app.documents.core.BuildConfig
import app.documents.core.network.storages.dropbox.models.request.AccountRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Credentials
import retrofit2.HttpException
import retrofit2.Response

sealed class DropboxResponse {
    class Success(val response: Any) : DropboxResponse()
    class Error(val error: Throwable) : DropboxResponse()
}

class DropboxLoginProvider(
    private val dropBoxLoginService: DropboxLoginService,
    private val dropboxErrorHandler: BehaviorRelay<DropboxResponse.Error>? = null
) {

    fun getUserInfo(token: String, request: AccountRequest): Single<DropboxResponse> {
        return dropBoxLoginService.getUserInfo(token, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getRefreshToken(auth: String, map: Map<String, String>): Single<DropboxResponse> {
        return dropBoxLoginService.getRefreshToken(auth, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateRefreshToken(map: Map<String, String>): Single<DropboxResponse> {
        val credentials = Credentials.basic(BuildConfig.DROP_BOX_COM_CLIENT_ID, BuildConfig.DROP_BOX_COM_CLIENT_SECRET)
        return dropBoxLoginService.updateRefreshToken(credentials, map)
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