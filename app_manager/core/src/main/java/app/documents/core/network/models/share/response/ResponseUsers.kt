package app.documents.core.network.models.share.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.login.User
import kotlinx.serialization.Serializable

@Serializable
data class ResponseUsers(val response: List<User> = emptyList()) : Base()