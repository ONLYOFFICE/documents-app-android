package app.documents.core.network.storages.googledrive.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ShareRequest(
    val role: String = "",
    val type: String = ""
)
