package app.editors.manager.onedrive

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemHashes(
    val quickXorHash: String = "",
    val sha1Hash: String = "",
    val sha256Hash: String = "",
)
