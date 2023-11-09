package app.documents.core.network.manager.models.response

import app.documents.core.network.manager.models.conversion.ConversionStatus
import kotlinx.serialization.Serializable

@Serializable
class ResponseConversionStatus : BaseListResponse<ConversionStatus>()