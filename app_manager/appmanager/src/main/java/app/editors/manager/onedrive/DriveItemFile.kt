package app.editors.manager.onedrive

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemFile(
    val mimeType: String = "",
    val hashes: DriveItemHashes
)
