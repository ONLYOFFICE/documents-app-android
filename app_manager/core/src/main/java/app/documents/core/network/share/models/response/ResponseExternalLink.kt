package app.documents.core.network.share.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.share.models.ExternalLink
import kotlinx.serialization.Serializable

@Serializable
class ResponseExternalLink(val response: List<ExternalLink> = emptyList()) : BaseResponse()