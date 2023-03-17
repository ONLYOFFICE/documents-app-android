package app.documents.core.network.storages.onedrive.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemFile(
    val mimeType: String = "",
    val hashes: DriveItemHashes? = null
)
