package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateSharedLink(
    val access: Int,
    val internal: Boolean,
    val primary: Boolean
)