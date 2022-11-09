package app.documents.core.network.models.room

import kotlinx.serialization.Serializable

@Serializable
data class RequestArchive(
    val deleteAfter: Boolean = false
)