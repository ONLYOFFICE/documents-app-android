package app.documents.core.network.login.models

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String? = "",
    val expires: String? = "",
    val sms: Boolean? = false,
    val phoneNoise: String? = "",
    val tfa: Boolean? = false,
    val tfaKey: String? = "",
)