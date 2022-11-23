package app.documents.core.network.login.models.response

import app.documents.core.network.common.models.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAllSettings(val response: app.documents.core.network.login.models.AllSettings) : BaseResponse()