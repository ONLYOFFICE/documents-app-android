package app.editors.manager.onedrive

import android.util.Log
import app.editors.manager.managers.providers.IOneDriveServiceProvider
import app.editors.manager.managers.providers.OneDriveResponse
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response

class OneDriveServiceProvider(
    private val oneDriveService: OneDriveService,
    private val oneDriveErrorHandler: BehaviorRelay<OneDriveResponse.Error>? = null
): IOneDriveServiceProvider {

    override fun authorization(parameters: Map<String, String>): Single<OneDriveResponse> {
        return oneDriveService.authorization(parameters)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun userInfo(): Single<OneDriveResponse> {
        return oneDriveService.getUserInfo()
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getFiles(): Single<OneDriveResponse> {
        return oneDriveService.getFiles()
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRoot(): Single<OneDriveResponse> {
        return oneDriveService.getRoot()
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun download(itemId: String): Single<OneDriveResponse> {
        return oneDriveService.download(itemId)
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