package app.editors.manager.storages.onedrive.mvp.models.explorer

import kotlinx.serialization.Serializable


@Serializable
data class DriveItemParentReference(
    val driveId: String = "",
    val driveType: String = "",
    val id: String = "",
    val name: String? = null,
    val path: String = ""
)
