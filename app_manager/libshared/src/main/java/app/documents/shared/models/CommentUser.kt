package app.documents.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class CommentUser(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String
)