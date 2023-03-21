package app.documents.core.network.login.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.login.models.Capabilities
import kotlinx.serialization.Serializable

@Serializable
data class ResponseCapabilities(val response: Capabilities) : BaseResponse()