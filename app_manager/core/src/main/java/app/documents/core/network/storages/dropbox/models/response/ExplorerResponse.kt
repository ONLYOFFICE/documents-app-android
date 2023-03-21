package app.documents.core.network.storages.dropbox.models.response

import app.documents.core.network.storages.dropbox.models.explorer.DropboxItem
import kotlinx.serialization.Serializable

@Serializable
data class ExplorerResponse(
    val entries: List<DropboxItem> = emptyList(),
    val cursor: String = "",
    val has_more: Boolean = false
)
