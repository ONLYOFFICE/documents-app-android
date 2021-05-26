package app.documents.core.network.models.login.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.login.Settings
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSettings(val response: Settings) : Base()