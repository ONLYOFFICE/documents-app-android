package app.editors.manager.dropbox.mvp.models.response

import app.editors.manager.dropbox.mvp.models.explorer.DropboxItem
import kotlinx.serialization.Serializable

@Serializable
data class ExplorerResponse(
    val entries: List<DropboxItem> = emptyList(),
    val cursor: String = "",
    val has_more: Boolean = false
)
