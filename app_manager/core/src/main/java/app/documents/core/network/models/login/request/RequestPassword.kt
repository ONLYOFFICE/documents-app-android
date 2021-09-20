package app.documents.core.network.models.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestPassword (val portal: String = "", val email: String = "") {
}
