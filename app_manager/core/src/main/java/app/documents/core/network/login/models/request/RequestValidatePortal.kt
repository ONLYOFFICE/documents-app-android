package app.documents.core.network.login.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestValidatePortal(val portalName: String)