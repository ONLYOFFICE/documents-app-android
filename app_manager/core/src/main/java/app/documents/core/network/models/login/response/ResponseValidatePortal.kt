package app.documents.core.network.models.login.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseValidatePortal(
    val message: String = "",
    val error: String = "",
    val variants: Array<String> = emptyArray(),
)