package app.editors.manager.storages.googledrive.mvp.models.resonse

import app.editors.manager.storages.googledrive.mvp.models.GoogleDriveFile
import kotlinx.serialization.Serializable

@Serializable
data class GoogleDriveExplorerResponse(
    val nexPageToken: String = "",
    val files: List<GoogleDriveFile> = emptyList()
)
