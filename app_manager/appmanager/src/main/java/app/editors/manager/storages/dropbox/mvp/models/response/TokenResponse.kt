package app.editors.manager.storages.dropbox.mvp.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val uid: String,
    @SerialName("access_token")val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String,
    val scope: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("account_id") val accountId: String
)

@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token")val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long
)