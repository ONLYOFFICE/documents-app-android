package app.editors.manager.storages.onedrive.mvp.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemOperation(
    val application: DriveItemUtil? = null,
    val user: DriveItemUtil
)
