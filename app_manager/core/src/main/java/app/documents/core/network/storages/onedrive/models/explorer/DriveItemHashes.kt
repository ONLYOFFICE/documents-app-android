package app.documents.core.network.storages.onedrive.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemHashes(
    val quickXorHash: String = "",
    val sha1Hash: String = "",
    val sha256Hash: String = "",
)
