package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestUpdatePublic(
    val id: String,
    val public: Boolean
)
