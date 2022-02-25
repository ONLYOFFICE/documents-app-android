package app.editors.manager.storages.onedrive.mvp.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemUtil(
    val displayName: String = "",
    val id: String = ""
)
