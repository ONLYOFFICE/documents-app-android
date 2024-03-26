package app.documents.core.model.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestNumber(
    val mobilePhone: String,
    val userName: String = "",
    val password: String = "",
    val provider: String = "",
    val accessToken: String = ""
)