package app.documents.core.network.storages.dropbox.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchMatchType(
    @SerialName(".tag") val tag: String = ""
)
