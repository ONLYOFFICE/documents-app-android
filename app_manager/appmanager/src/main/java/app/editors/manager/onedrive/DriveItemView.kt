package app.editors.manager.onedrive

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemView(
    val viewType: String = "",
    val sortBy: String = "",
    val sortOrder: String = ""
)
