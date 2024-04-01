package app.documents.core.model.login.response

import app.documents.core.model.login.GoogleUser
import kotlinx.serialization.Serializable

@Serializable
data class GoogleUserResponse(val user: GoogleUser)
