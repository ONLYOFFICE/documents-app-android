package app.documents.core.network.login.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestPassword (val portal: String = "", val email: String = "") {
}
