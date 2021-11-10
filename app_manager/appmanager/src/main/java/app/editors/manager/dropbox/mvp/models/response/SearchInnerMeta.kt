package app.editors.manager.dropbox.mvp.models.response

import app.editors.manager.dropbox.mvp.models.explorer.DropboxItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchInnerMeta(
    @SerialName(".tag") val tag: String = "",
    val metadata: DropboxItem? = null
)
