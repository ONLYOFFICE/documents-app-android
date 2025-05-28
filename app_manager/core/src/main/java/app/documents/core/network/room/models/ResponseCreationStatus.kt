package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class ResponseCreationStatus(
    val templateId: String = "",
    val roomId: String = "",
    val progress: Double,
    val isCompleted: Boolean,
    val error: String? = null
)
