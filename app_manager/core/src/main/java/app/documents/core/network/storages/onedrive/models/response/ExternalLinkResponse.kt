package app.documents.core.network.storages.onedrive.models.response

import app.documents.core.network.storages.onedrive.models.other.Link
import kotlinx.serialization.Serializable

@Serializable
data class ExternalLinkResponse(
    val id: String = "",
    val roles: List<String> = emptyList(),
    val link: Link? = null
)
