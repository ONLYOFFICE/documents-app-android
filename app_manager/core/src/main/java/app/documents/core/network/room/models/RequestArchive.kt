package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestArchive(
    val deleteAfter: Boolean = false
)