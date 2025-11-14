package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestShareItem(
    var shareTo: String,
    var access: Int
)