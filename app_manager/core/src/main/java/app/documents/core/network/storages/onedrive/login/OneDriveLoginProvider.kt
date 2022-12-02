package app.documents.core.network.storages.onedrive.login

import app.documents.core.BuildConfig
import app.documents.core.network.common.contracts.StorageContract
import app.documents.core.network.storages.onedrive.api.OneDriveResponse
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response

class OneDriveLoginProvider(
    private val oneDriveLoginService: OneDriveLoginService,
    private val oneDriveErrorHandler: BehaviorRelay<OneDriveResponse.Error>? = null
) {

    fun refreshToken(refreshToken: String): Single<OneDriveResponse> {
        val params = mapOf(
            StorageContract.ARG_CLIENT_ID to BuildConfig.ONE_DRIVE_COM_CLIENT_ID,
            StorageContract.ARG_SCOPE to StorageContract.OneDrive.VALUE_SCOPE,
            StorageContract.ARG_REDIRECT_URI to BuildConfig.ONE_DRIVE_COM_REDIRECT_URL,
            StorageContract.ARG_GRANT_TYPE to StorageContract.OneDrive.VALUE_GRANT_TYPE_REFRESH,
            StorageContract.ARG_CLIENT_SECRET to BuildConfig.ONE_DRIVE_COM_CLIENT_SECRET,
            StorageContract.ARG_REFRESH_TOKEN to refreshToken
        )
        return oneDriveLoginService.getToken(params)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getToken(code: String): Single<OneDriveResponse> {
        val params = mapOf(
            StorageContract.ARG_CLIENT_ID to BuildConfig.ONE_DRIVE_COM_CLIENT_ID,
            StorageContract.ARG_SCOPE to StorageContract.OneDrive.VALUE_SCOPE,
            StorageContract.ARG_REDIRECT_URI to BuildConfig.ONE_DRIVE_COM_REDIRECT_URL,
            StorageContract.ARG_GRANT_TYPE to StorageContract.OneDrive.VALUE_GRANT_TYPE_AUTH,
            StorageContract.ARG_CLIENT_SECRET to BuildConfig.ONE_DRIVE_COM_CLIENT_SECRET,
            StorageContract.ARG_CODE to code
        )
        return oneDriveLoginService.getToken(params)
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