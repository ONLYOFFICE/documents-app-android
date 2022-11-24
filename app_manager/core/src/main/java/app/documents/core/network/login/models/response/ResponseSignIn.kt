package app.documents.core.network.login.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.login.models.Token
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSignIn(val response: Token) : BaseResponse()