package app.editors.manager.onedrive.mvp.models.explorer

import kotlinx.serialization.Serializable


@Serializable
data class DriveItemFileSystemInfo(
    val createdDateTime: String = "",
    val lastModifiedDateTime: String = ""
)
