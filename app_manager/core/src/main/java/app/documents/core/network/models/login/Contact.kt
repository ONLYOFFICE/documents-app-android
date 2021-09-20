package app.documents.core.network.models.login

import kotlinx.serialization.Serializable

@Serializable
class Contact(
    val type: String = "",
    val value: String = ""
)
