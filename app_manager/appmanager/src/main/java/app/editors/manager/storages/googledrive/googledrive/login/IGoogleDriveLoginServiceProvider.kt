package app.editors.manager.storages.googledrive.googledrive.login

import io.reactivex.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class GoogleDriveResponse {
    class Success(val response: Any) : GoogleDriveResponse()
    class Error(val error: Throwable) : GoogleDriveResponse()
}

@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
)

interface IGoogleDriveLoginServiceProvider {
    fun getToken(map: Map<String, String>): Single<TokenResponse>
}