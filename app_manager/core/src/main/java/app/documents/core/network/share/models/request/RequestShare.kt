package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestShare(
    val share: List<RequestShareItem> = emptyList(),
    val isNotify: Boolean = false,
    val sharingMessage: String = "",
)