package app.editors.manager.storages.dropbox.mvp.models.request

import kotlinx.serialization.Serializable

enum class TokenType(val type: String) {
    GET("authorization_code"), REFRESH("refresh_token")
}

@Serializable
data class TokenRequest(
    val code: String,
    val grant_type: String = TokenType.GET.type,
    val redirect_uri: String = "https://service.onlyoffice.com/oauth2.aspx"
)

@Serializable
data class TokenRefreshRequest(
    val refresh_token: String,
    val grant_type: String = TokenType.REFRESH.type,
)