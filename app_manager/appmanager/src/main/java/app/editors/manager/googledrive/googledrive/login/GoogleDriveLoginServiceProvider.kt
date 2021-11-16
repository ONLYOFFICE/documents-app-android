package app.editors.manager.googledrive.googledrive.login

import com.jakewharton.rxrelay2.BehaviorRelay
import retrofit2.HttpException
import retrofit2.Response

class GoogleDriveLoginServiceProvider (
    private val googleDriveLoginService: GoogleDriveLoginService,
    private val googleDriveErrorHandle: BehaviorRelay<GoogleDriveResponse.Error>? = null
): IGoogleDriveLoginServiceProvider {



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