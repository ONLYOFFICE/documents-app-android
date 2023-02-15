package app.documents.core.network.storages.onedrive.models.explorer

import kotlinx.serialization.Serializable


@Serializable
data class DriveItemFileSystemInfo(
    val createdDateTime: String = "",
    val lastModifiedDateTime: String = ""
)
