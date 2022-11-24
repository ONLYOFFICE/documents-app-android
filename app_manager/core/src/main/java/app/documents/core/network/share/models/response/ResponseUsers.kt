package app.documents.core.network.share.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.login.models.User
import kotlinx.serialization.Serializable

@Serializable
data class ResponseUsers(val response: List<User> = emptyList()) : BaseResponse()