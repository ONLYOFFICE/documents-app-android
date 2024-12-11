package app.documents.core.network.room.models

import app.documents.core.network.manager.models.explorer.Lifetime
import kotlinx.serialization.Serializable

@Serializable
data class RequestEditRoom(

    val title: String? = null,

    val quota: Long? = null,

    val lifetime: Lifetime? = null,

    val denyDownload: Boolean? = null,

    val indexing: Boolean? = null,
)