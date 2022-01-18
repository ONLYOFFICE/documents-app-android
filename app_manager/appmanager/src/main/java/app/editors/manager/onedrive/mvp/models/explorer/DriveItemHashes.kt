package app.editors.manager.onedrive.mvp.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemHashes(
    val quickXorHash: String = "",
    val sha1Hash: String = "",
    val sha256Hash: String = "",
)
