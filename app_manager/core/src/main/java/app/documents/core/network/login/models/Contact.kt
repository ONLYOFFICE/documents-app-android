package app.documents.core.network.login.models

import kotlinx.serialization.Serializable

@Serializable
class Contact(
    val type: String = "",
    val value: String = ""
)
