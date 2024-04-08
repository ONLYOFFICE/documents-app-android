package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateSharedLink(
    val access: Int = 2,
    val internal: Boolean = false,
    val primary: Boolean = false,
)