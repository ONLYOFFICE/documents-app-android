package app.editors.manager.googledrive.mvp.models

import app.editors.manager.googledrive.mvp.models.resonse.FileCapabilities
import kotlinx.serialization.Serializable


@Serializable
data class GoogleDriveFile(
    val id: String = "",
    val name: String = "",
    val mimeType: String = "",
    val description: String = "",
    val parents: List<String> = emptyList(),
    val webViewLink: String = "",
    val modifiedTime: String = "",
    val createdTime: String = "",
    val capabilities: FileCapabilities? = null,
    val size: String = ""
)
