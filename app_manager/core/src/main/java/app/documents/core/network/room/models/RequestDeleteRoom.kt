package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestDeleteRoom(
    val deleteAfter: Boolean = false
)