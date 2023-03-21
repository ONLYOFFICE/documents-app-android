package app.documents.core.network.login.models.response

import app.documents.core.network.login.models.Tenant
import kotlinx.serialization.Serializable

@Serializable
class ResponseRegisterPortal(val reference: String, val tenant: Tenant)