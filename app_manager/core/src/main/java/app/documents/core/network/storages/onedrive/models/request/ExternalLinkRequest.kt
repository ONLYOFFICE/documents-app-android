package app.documents.core.network.storages.onedrive.models.request

import kotlinx.serialization.Serializable


@Serializable
data class ExternalLinkRequest(
    val type: String = "",
    val scope: String = ""
)

