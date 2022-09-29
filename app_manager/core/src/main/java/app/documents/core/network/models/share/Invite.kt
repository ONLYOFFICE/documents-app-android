package app.documents.core.network.models.share

import kotlinx.serialization.Serializable

@Serializable
data class Invite(
    val email: String,
    val success: Boolean
)