package app.documents.core.model.login.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnedriveSignInResponse(
    @SerialName("token_type") val tokenType: String = "",
    @SerialName("scope") val scope: String = "",
    @SerialName("expires_in") val expiresIn: String = "",
    @SerialName("ext_expires_in") val extExpiresIn: String = "",
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("refresh_token") val refreshToken: String = ""
)
