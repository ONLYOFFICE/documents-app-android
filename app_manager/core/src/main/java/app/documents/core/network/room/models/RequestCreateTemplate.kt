package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateTemplate(
    val roomId: String,
    val title: String? = null,
    val tags: List<String>? = null,
    val quota: Long? = null,
    val public: Boolean? = null,
    val copylogo: Boolean? = null,
    val color: String? = null,
    val logo: RequestSetLogo? = null,
    val share: List<String>? = null,
    val groups: List<String>? = null
)