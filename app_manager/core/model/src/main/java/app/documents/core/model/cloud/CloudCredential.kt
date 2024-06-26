package app.documents.core.model.cloud

import kotlinx.serialization.Serializable


@Serializable
data class CloudCredential(
    val accountId: String,
    val portalId: String
)