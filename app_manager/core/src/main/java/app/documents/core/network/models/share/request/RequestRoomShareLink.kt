package app.documents.core.network.models.share.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestRoomShareLink(val access: Int = 0, val key: String = "")