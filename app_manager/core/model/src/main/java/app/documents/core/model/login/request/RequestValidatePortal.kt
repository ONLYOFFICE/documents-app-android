package app.documents.core.model.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestValidatePortal(val portalName: String)