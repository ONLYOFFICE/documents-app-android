package app.editors.manager.dropbox.dropbox.api

import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.mvp.models.request.ExplorerRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response

class DropboxServiceProvider(
    private val dropBoxService: DropboxService,
    private val dropboxErrorHandler: BehaviorRelay<DropboxResponse.Error>? = null
): IDropboxServiceProvider {

    override fun getFiles(request: ExplorerRequest): Single<DropboxResponse> {
        return dropBoxService.getFiles(request)
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