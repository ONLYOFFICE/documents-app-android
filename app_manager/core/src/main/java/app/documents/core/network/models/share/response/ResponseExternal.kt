package app.documents.core.network.models.share.response

import app.documents.core.network.models.Base
import kotlinx.serialization.Serializable

@Serializable
data class ResponseExternal(val response: String = "") : Base()