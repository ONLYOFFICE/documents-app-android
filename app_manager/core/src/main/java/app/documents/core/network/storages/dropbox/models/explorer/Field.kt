package app.documents.core.network.storages.dropbox.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class Field(
    val name: String = "",
    val value: String = ""
)
