package app.documents.core.network.login.models.response

import app.documents.core.network.common.models.Base
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSignIn(val response: app.documents.core.network.login.models.Token) : Base()