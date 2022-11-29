package app.documents.core.network.storages.googledrive.models.resonse

import app.documents.core.network.storages.googledrive.models.GoogleDriveFile
import kotlinx.serialization.Serializable

@Serializable
data class GoogleDriveExplorerResponse(
    val nexPageToken: String = "",
    val files: List<GoogleDriveFile> = emptyList()
)
