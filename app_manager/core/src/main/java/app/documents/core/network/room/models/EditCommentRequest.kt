package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class EditCommentRequest(
    val version: Int,
    val comment: String
)
