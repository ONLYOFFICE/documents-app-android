package app.editors.manager.onedrive.mvp.models.request

import app.editors.manager.onedrive.mvp.models.explorer.DriveItemFolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateFolderRequest(
    val name: String = "",
    val folder: DriveItemFolder? = null,
    @SerialName("@microsoft.graph.conflictBehavior") val conflictBehavior: String = ""
)
