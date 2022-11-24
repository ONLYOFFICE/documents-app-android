package app.documents.core.network.login.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.login.models.User
import kotlinx.serialization.Serializable

@Serializable
data class ResponseUser(val response: User) : BaseResponse()