package app.documents.core.network.share.models

import kotlinx.serialization.Serializable

@Serializable
data class Invite(
    val email: String,
    val success: Boolean
)