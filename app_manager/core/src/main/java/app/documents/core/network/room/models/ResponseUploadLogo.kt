package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class ResponseUploadLogo(val success: Boolean, val data: String = "")