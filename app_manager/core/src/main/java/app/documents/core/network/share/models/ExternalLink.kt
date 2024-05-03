package app.documents.core.network.share.models

import kotlinx.serialization.Serializable

@Serializable
data class ExternalLink(
    val access: Int,
    val isLocked: Boolean = false,
    val isOwner: Boolean = false,
    val canEditAccess: Boolean = false,
    val subjectType: Int? = null,
    val sharedTo: ExternalLinkSharedTo
)