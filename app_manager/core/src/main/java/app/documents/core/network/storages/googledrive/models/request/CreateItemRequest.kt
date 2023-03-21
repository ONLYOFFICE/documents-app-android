package app.documents.core.network.storages.googledrive.models.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val name: String = "",
    val mimeType: String = "",
    val parents: List<String> = emptyList(),
)
