package app.documents.core.network.share.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.share.models.Share
import kotlinx.serialization.Serializable

@Serializable
data class ResponseShare(val response: List<Share> = emptyList()) : BaseResponse()