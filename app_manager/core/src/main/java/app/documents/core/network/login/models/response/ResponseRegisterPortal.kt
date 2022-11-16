package app.documents.core.network.login.models.response

import kotlinx.serialization.Serializable

@Serializable
class ResponseRegisterPortal(
    val reference: String,
    val tenant: app.documents.core.network.login.models.Tenant
)