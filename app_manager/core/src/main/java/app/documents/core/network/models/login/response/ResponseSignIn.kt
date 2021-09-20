package app.documents.core.network.models.login.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.login.Token
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSignIn(val response: Token) : Base()