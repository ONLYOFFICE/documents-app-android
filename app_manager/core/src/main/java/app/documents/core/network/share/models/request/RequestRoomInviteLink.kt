package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestRoomInviteLink(
    val access: Int,
    val linkId: String?,
    val title: String
)