package app.documents.core.network.room.models

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.share.models.ExternalLink
import kotlinx.serialization.Serializable

@Serializable
data class ResponseUpdateExternalLink(val response: ExternalLink) : BaseResponse()