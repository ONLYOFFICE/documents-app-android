package app.editors.manager.storages.dropbox.mvp.models.response

import app.editors.manager.storages.dropbox.mvp.models.explorer.DropboxItem
import kotlinx.serialization.Serializable

@Serializable
data class ExplorerResponse(
    val entries: List<DropboxItem> = emptyList(),
    val cursor: String = "",
    val has_more: Boolean = false
)
