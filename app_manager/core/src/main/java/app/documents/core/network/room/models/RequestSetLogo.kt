package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestSetLogo(val logo: String, val width: Int = 0, val height: Int = 0)