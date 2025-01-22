package app.documents.core.network.room.models

import app.documents.core.model.cloud.Access
import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateExternalLink(
    val access: Int = Access.Read.code,
    val denyDownload: Boolean = false,
    val expirationDate: String? = null,
    val linkType: Int = 1,
    val password: String? = null,
    val title: String
)