package app.documents.core.model.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestPassword(val portal: String = "", val email: String = "")