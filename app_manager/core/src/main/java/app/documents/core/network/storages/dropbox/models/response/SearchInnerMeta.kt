package app.documents.core.network.storages.dropbox.models.response

import app.documents.core.network.storages.dropbox.models.explorer.DropboxItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchInnerMeta(
    @SerialName(".tag") val tag: String = "",
    val metadata: DropboxItem? = null
)
