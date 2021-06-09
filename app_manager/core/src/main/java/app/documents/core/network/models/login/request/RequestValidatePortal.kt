package app.documents.core.network.models.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestValidatePortal(val portalName: String)