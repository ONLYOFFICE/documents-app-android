package app.documents.core.network.models.login.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.login.AllSettings
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAllSettings(val response: AllSettings) : Base()