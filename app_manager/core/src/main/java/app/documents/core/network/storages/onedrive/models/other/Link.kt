package app.documents.core.network.storages.onedrive.models.other

import kotlinx.serialization.Serializable

@Serializable
data class Link(
    val type: String = "",
    val scope: String = "",
    val webUrl: String = "",
    val application: Application? = null
)
