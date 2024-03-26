package app.documents.core.model.login.response

import app.documents.core.model.login.Tenant
import kotlinx.serialization.Serializable

@Serializable
class ResponseRegisterPortal(val reference: String, val tenant: Tenant)