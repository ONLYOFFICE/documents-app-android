package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestRoomShare(
    val invitations: List<RequestInvitation>,
    val notify: Boolean = false,
    val message: String = ""
)

interface RequestInvitation

@Serializable
data class Invitation(
    val email: String? = null,
    val id: String? = null,
    val access: Int
) : RequestInvitation

@Serializable
data class EmailInvitation(
    val email: String,
    val access: Int
) : RequestInvitation


@Serializable
data class UserIdInvitation(
    val id: String,
    val access: Int
) : RequestInvitation

