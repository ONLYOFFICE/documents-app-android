package app.editors.manager.googledrive.googledrive.api

import app.editors.manager.googledrive.googledrive.login.GoogleDriveResponse
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response

class GoogleDriveServiceProvider(
    private val googleDriveServiceProvider: GoogleDriveService,
    private val googleDriveErrorHandle: BehaviorRelay<GoogleDriveResponse.Error>? = null
): IGoogleDriveServiceProvider {

    override fun getFiles(map: Map<String, String>): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.getFiles(map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> fetchResponse(response: Response<T>): GoogleDriveResponse {
        return if (response.isSuccessful && response.body() != null) {
            GoogleDriveResponse.Success(response.body()!!)
        } else {
            val error = GoogleDriveResponse.Error(HttpException(response))
            googleDriveErrorHandle?.accept(error)
            return error
        }
    }
}