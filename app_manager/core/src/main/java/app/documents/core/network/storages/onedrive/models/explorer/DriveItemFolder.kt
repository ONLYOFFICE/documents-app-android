package app.documents.core.network.storages.onedrive.models.explorer

import kotlinx.serialization.Serializable


@Serializable
data class DriveItemFolder(
    val childCount: String = "",
    val view: DriveItemView? = null
)
