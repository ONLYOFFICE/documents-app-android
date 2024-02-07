package app.documents.core.network.share.models

import kotlinx.serialization.Serializable

@Serializable
data class ExternalLink(
    val access: Int,
    val isLocked: Boolean,
    val isOwner: Boolean,
    val canEditAccess: Boolean,
    val sharedTo: ExternalLinkSharedTo
)