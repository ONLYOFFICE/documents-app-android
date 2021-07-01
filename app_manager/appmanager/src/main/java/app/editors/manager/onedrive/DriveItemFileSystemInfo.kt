package app.editors.manager.onedrive

import kotlinx.serialization.Serializable


@Serializable
data class DriveItemFileSystemInfo(
    val createdDateTime: String = "",
    val lastModifiedDateTime: String = ""
)
