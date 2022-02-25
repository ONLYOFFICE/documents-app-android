package app.editors.manager.storages.onedrive.mvp.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemFile(
    val mimeType: String = "",
    val hashes: DriveItemHashes? = null
)
