package app.editors.manager.storages.dropbox.mvp.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchMatchType(
    @SerialName(".tag") val tag: String = ""
)
