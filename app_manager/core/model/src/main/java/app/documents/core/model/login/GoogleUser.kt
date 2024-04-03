package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class GoogleUser(
    val kind: String = "",
    val displayName: String = "",
    val photoLink: String = "",
    val me: Boolean = false,
    val permissionId: String = "",
    val emailAddress: String = ""
)
