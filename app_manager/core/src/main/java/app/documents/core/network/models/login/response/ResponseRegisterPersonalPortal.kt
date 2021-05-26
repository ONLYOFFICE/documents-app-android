package app.documents.core.network.models.login.response

import app.documents.core.network.models.Base
import kotlinx.serialization.Serializable

@Serializable
data class ResponseRegisterPersonalPortal(val response: String?) : Base(count = null)