package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestRoomOwner(val userId: String, val foldersIds: List<String>)