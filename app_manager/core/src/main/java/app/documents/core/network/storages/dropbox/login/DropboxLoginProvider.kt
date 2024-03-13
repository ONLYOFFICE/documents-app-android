package app.documents.core.network.storages.dropbox.login

import app.documents.core.BuildConfig
import app.documents.core.network.common.contracts.StorageContract
import app.documents.core.model.login.response.RefreshTokenResponse
import app.documents.core.model.login.response.TokenResponse
import app.documents.core.network.storages.dropbox.models.response.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import retrofit2.HttpException
import retrofit2.Response

sealed class DropboxResponse {
    class Success(val response: Any) : DropboxResponse()
    class Error(val error: Throwable) : DropboxResponse()
}

class DropboxLoginProvider(private val dropBoxLoginService: DropboxLoginService) {

    suspend fun getAccessToken(code: String): TokenResponse = withContext(Dispatchers.IO) {
        val params = mapOf(
            StorageContract.ARG_CODE to code,
            StorageContract.ARG_GRANT_TYPE to StorageContract.DropBox.VALUE_GRANT_TYPE,
            StorageContract.ARG_REDIRECT_URI to BuildConfig.DROP_BOX_COM_REDIRECT_URL,
            StorageContract.ARG_CLIENT_ID to BuildConfig.DROP_BOX_COM_CLIENT_ID,
            StorageContract.ARG_CLIENT_SECRET to BuildConfig.DROP_BOX_COM_CLIENT_SECRET
        )
        fetchResponse(dropBoxLoginService.getAccessToken(params))
    }

    suspend fun getUserInfo(token: String): UserResponse = withContext(Dispatchers.IO) {
        fetchResponse(dropBoxLoginService.getUserInfo("Bearer $token"))
    }

    suspend fun updateRefreshToken(refreshToken: String): RefreshTokenResponse = withContext(Dispatchers.IO) {
        val credentials = Credentials.basic(BuildConfig.DROP_BOX_COM_CLIENT_ID, BuildConfig.DROP_BOX_COM_CLIENT_SECRET)
        val params = mapOf(
            StorageContract.ARG_GRANT_TYPE to StorageContract.ARG_REFRESH_TOKEN,
            StorageContract.ARG_REFRESH_TOKEN to refreshToken
        )
        fetchResponse(dropBoxLoginService.updateRefreshToken(credentials, params))
    }

    private fun <T> fetchResponse(response: Response<T>): T {
        val body = response.body()
        return if (response.isSuccessful && body != null) body
        else throw HttpException(response)
    }

}