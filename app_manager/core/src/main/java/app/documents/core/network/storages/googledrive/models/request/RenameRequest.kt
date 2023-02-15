package app.documents.core.network.storages.googledrive.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RenameRequest(
    val name: String = ""
)
