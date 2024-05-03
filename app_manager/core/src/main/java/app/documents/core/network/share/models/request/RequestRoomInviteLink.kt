package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

private const val INVITE_LINK_TITLE = "Invite"

@Serializable
data class RequestAddInviteLink(
    val access: Int,
    val title: String = INVITE_LINK_TITLE
)

@Serializable
data class RequestRemoveInviteLink(
    val access: Int = 0,
    val linkId: String?,
    val title: String = INVITE_LINK_TITLE
)