package app.documents.core.network.room.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class RequestEditRoom(

    @Transient
    val title: String? = null,

    @Transient
    val quota: Long? = null
)