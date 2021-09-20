package app.documents.core.network.models.share.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestShareItem(
    var shareTo: String = "",
    var access: String = ""
)