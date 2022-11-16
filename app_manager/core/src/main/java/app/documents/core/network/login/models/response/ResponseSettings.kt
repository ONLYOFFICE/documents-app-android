package app.documents.core.network.login.models.response

import app.documents.core.network.common.models.Base
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSettings(val response: app.documents.core.network.login.models.Settings) : Base()