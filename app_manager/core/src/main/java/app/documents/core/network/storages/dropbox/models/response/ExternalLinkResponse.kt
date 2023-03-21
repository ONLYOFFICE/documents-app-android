package app.documents.core.network.storages.dropbox.models.response

import app.documents.core.network.storages.dropbox.models.explorer.DropboxItem
import kotlinx.serialization.Serializable

@Serializable
data class ExternalLinkResponse(
    val metadata: DropboxItem? = null,
    val link: String = ""
)
