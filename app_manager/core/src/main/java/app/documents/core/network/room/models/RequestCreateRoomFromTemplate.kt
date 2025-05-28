package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateRoomFromTemplate(
    val templateId: String,
    val title: String,
    val tags: List<String>? = null,
    val quota: Long? = null,
    val copylogo: Boolean? = null,
    val color: String? = null,
    val logo: RequestSetLogo? = null
)
