package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class Tenant(
    val created: String? = null,
    val domain: String? = null,
    val language: String? = null,
    val ownerId: String? = null,
    val portalName: String? = null,
    val status: String? = null,
    val tenantId: Int? = null,
    val timeZoneName: String? = null,
)