package app.documents.core.network.models.login.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.login.Capabilities
import kotlinx.serialization.Serializable

@Serializable
data class ResponseCapabilities(
    val response: Capabilities
) : Base()