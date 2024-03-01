package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String = "",
    val expires: String = "",
    val sms: Boolean = false,
    val phoneNoise: String = "",
    val tfa: Boolean = false,
    val tfaKey: String = "",
)