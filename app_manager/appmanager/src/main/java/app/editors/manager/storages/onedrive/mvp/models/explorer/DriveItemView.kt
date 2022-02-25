package app.editors.manager.storages.onedrive.mvp.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemView(
    val viewType: String = "",
    val sortBy: String = "",
    val sortOrder: String = ""
)
