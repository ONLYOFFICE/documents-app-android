package app.documents.core.network.models.share.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.share.Share
import kotlinx.serialization.Serializable

@Serializable
data class ResponseShare(val response: List<Share> = emptyList()) : Base()