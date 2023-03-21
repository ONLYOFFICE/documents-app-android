package app.documents.core.network.login.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.login.models.AllSettings
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAllSettings(val response: AllSettings) : BaseResponse()