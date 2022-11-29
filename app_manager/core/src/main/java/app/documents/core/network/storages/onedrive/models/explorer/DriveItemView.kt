package app.documents.core.network.storages.onedrive.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class DriveItemView(
    val viewType: String = "",
    val sortBy: String = "",
    val sortOrder: String = ""
)
