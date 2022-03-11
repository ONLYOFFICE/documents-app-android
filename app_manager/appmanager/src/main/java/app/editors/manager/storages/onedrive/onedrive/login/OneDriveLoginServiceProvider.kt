package app.editors.manager.storages.onedrive.onedrive.login

import android.util.Log
import app.editors.manager.storages.onedrive.onedrive.api.OneDriveResponse
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response

class OneDriveLoginServiceProvider(
    private val oneDriveService: OneDriveLoginService,
    private val oneDriveErrorHandler: BehaviorRelay<OneDriveResponse.Error>? = null
): IOneDriveLoginServiceProvider {

    override fun getUserInfo(token: String): Single<OneDriveResponse> {
        return oneDriveService.getUserInfo(token)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

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