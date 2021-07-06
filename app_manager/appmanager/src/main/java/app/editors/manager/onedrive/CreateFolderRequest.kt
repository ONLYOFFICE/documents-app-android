package app.editors.manager.onedrive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateFolderRequest(
    val name: String = "",
    val folder: DriveItemFolder? = null,
    @SerialName("@microsoft.graph.conflictBehavior") val conflictBehavior: String = ""
)
