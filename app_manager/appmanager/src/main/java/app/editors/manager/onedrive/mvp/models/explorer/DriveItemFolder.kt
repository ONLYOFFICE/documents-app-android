package app.editors.manager.onedrive.mvp.models.explorer

import kotlinx.serialization.Serializable


@Serializable
data class DriveItemFolder(
    val childCount: String = "",
    val view: DriveItemView? = null
)
