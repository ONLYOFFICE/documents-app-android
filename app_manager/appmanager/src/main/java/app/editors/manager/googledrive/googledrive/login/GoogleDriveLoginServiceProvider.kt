package app.editors.manager.googledrive.googledrive.login

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class GoogleDriveLoginServiceProvider (
    private val googleDriveLoginService: GoogleDriveLoginService,
    private val googleDriveErrorHandle: BehaviorRelay<GoogleDriveResponse.Error>? = null
): IGoogleDriveLoginServiceProvider {

    override fun getToken(map: Map<String, String>): Single<Response<ResponseBody>> {
        return googleDriveLoginService.getToken(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getUserInfo(token: String): Single<GoogleDriveResponse> {
        return googleDriveLoginService.getUserInfo(token)
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