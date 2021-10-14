package app.editors.manager.onedrive.onedrive.authorization

import app.editors.manager.onedrive.onedrive.OneDriveResponse
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response


class OneDriveAuthServiceProvider(
    private val oneDriveService: OneDriveAuthService,
    private val oneDriveErrorHandler: BehaviorRelay<OneDriveResponse.Error>? = null
) : IOneDriveAuthServiceProvider {

    override fun getToken(map: Map<String, String>): Single<OneDriveResponse> {
        return oneDriveService.getToken(map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }


    private fun <T> fetchResponse(response: Response<T>): OneDriveResponse {
        return if (response.isSuccessful && response.body() != null) {
            OneDriveResponse.Success(response.body()!!)
        } else {
            val error = OneDriveResponse.Error(HttpException(response))
            oneDriveErrorHandler?.accept(error)
            return error
        }
    }
}