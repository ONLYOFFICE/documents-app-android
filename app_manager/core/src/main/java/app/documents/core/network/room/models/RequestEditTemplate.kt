package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestEditTemplate(
    val title: String? = null,

    val quota: Long? = null,

    val tags: List<String>? = null,

    val logo: RequestSetLogo? = null,
)
