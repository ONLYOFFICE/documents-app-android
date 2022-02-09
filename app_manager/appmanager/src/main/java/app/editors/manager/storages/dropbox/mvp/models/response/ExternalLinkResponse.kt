package app.editors.manager.storages.dropbox.mvp.models.response

import app.editors.manager.storages.dropbox.mvp.models.explorer.DropboxItem
import kotlinx.serialization.Serializable

@Serializable
data class ExternalLinkResponse(
    val metadata: DropboxItem? = null,
    val link: String = ""
)
