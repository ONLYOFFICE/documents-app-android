package app.documents.core.network.models.login.response

import app.documents.core.network.models.login.Tenant
import kotlinx.serialization.Serializable

@Serializable
class ResponseRegisterPortal(
    val reference: String,
    val tenant: Tenant
)