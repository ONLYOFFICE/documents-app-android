package app.documents.core.network.models.room

import kotlinx.serialization.Serializable

@Serializable
data class RequestDeleteRoom(
    val deleteAfter: Boolean = false
)