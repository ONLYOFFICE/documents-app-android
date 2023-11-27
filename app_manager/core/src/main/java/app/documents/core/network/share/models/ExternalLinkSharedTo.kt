package app.documents.core.network.share.models

data class ExternalLinkSharedTo(
    val id: String,
    val title: String,
    val shareLink: String,
    val linkType: Int,
    val denyDownload: Boolean,
    val isExpired: Boolean,
    val primary: Boolean,
    val requestToken: String
)