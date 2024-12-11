package app.documents.core.network.room.models

import app.documents.core.network.manager.models.explorer.Lifetime
import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateRoom(

    val title: String,

    val roomType: Int,

    val quota: Long = -1,

    val lifetime: Lifetime? = null,

    val denyDownload: Boolean? = null,

    val indexing: Boolean? = null
)