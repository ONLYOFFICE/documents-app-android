package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestRoomShare(val invitations: List<Invitation>, val notify: Boolean = false, val message: String = "")

@Serializable
data class Invitation(val email: String? = null, val id: String? = null, val access: Int)
