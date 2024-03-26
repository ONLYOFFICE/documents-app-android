package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
class Contact(
    val type: String = "",
    val value: String = ""
)
