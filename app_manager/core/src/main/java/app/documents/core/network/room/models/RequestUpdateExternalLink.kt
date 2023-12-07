package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestUpdateExternalLink(
    val access: Int,
    val denyDownload: Boolean,
    val expirationDate: String?,
    val linkId: String,
    val linkType: Int,
    val password: String?,
    val title: String?
)