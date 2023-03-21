package app.documents.core.network.storages.onedrive.models.request

import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemFolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateFolderRequest(
    val name: String = "",
    val folder: DriveItemFolder? = null,
    @SerialName(OneDriveUtils.KEY_CONFLICT_BEHAVIOR) val conflictBehavior: String = ""
)
