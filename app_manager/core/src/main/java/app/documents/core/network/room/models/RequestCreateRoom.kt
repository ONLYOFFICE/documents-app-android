package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateRoom(
    val title: String,
    val roomType: Int
)
