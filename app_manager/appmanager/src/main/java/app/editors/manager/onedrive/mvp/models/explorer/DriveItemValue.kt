package app.editors.manager.onedrive.mvp.models.explorer

import app.editors.manager.onedrive.mvp.models.explorer.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class DriveItemValue(
    @SerialName("@odata.context") val context: String = "",
    val createdDateTime: String = "",
    val cTag: String = "",
    val eTag: String = "",
    val id: String = "",
    val lastModifiedDateTime: String = "",
    val name: String = "",
    val size: String ="",
    val webUrl: String = "",
    val reactions: DriveItemReactions? = null,
    val createdBy: DriveItemOperation,
    val lastModifiedBy: DriveItemOperation,
    val parentReference: DriveItemParentReference,
    val fileSystemInfo: DriveItemFileSystemInfo,
    val folder: DriveItemFolder? = null,
    val file: DriveItemFile? = null,
    val specialFolder: DriveItemSpecialFolder? = null
)
