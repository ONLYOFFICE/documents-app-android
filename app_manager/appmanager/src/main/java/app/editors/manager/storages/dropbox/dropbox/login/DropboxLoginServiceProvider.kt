package app.editors.manager.storages.dropbox.dropbox.login

import app.editors.manager.storages.dropbox.mvp.models.request.AccountRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response

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