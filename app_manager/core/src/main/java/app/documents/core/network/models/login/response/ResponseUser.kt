package app.documents.core.network.models.login.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.login.User
import kotlinx.serialization.Serializable

@Serializable
data class ResponseUser(val response: User) : Base()