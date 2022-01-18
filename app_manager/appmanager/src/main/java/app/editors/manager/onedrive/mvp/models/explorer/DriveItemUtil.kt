package app.editors.manager.onedrive.mvp.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemUtil(
    val displayName: String = "",
    val id: String = ""
)
