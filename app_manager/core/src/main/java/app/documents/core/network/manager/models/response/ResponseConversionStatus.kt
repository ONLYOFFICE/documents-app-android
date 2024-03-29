package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.conversion.ConversionStatus
import kotlinx.serialization.Serializable

@Serializable
class ResponseConversionStatus(val response: List<ConversionStatus> = emptyList()) : BaseResponse()