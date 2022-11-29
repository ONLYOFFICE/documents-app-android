package app.documents.core.network.storages.onedrive.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemOperation(
    val application: DriveItemUtil? = null,
    val user: DriveItemUtil
)
