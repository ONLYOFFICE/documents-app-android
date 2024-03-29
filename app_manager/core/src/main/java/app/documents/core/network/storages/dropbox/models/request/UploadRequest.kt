package app.documents.core.network.storages.dropbox.models.request

import kotlinx.serialization.Serializable

@Serializable
data class UploadRequest(
    val path: String = "",
    val mode: String = "",
    val autorename: Boolean = false,
    val mute: Boolean = false,
    val strict_conflict: Boolean = false
)
