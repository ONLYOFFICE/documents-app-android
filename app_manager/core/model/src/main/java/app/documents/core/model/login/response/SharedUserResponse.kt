package app.documents.core.model.login.response

import app.documents.core.model.login.User
import kotlinx.serialization.Serializable

@Serializable
data class SharedUserResponse(
    val user: User
)