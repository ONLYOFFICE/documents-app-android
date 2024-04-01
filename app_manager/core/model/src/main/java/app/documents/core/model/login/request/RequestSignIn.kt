package app.documents.core.model.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestSignIn(
    val userName: String = "",
    val password: String = "",
    val provider: String = "",
    val accessToken: String = "",
    val code: String = ""
)