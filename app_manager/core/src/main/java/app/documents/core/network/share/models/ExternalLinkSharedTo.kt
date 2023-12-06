package app.documents.core.network.share.models

import kotlinx.serialization.Serializable

@Serializable
data class ExternalLinkSharedTo(
    val id: String,
    val title: String,
    val shareLink: String,
    val linkType: Int,
    val password: String?,
    val denyDownload: Boolean,
    val isExpired: Boolean,
    val primary: Boolean,
    val requestToken: String,
    val expirationDate: String? // "2023-12-08T00:00:00.0000000+03:00"
)