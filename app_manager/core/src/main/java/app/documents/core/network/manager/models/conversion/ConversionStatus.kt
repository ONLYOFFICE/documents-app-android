package app.documents.core.network.manager.models.conversion

import kotlinx.serialization.Serializable

@Serializable
data class ConversionStatus(
    val id: String? = null,
    val progress: Int = 0,
    val source: String? = null,
    val error: String? = null,
    val processed: String? = null
)