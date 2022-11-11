package app.documents.core.network.models.room

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateRoom(
    val title: String,
    val roomType: Int
)
