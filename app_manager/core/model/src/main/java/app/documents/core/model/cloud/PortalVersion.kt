package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
data class PortalVersion(
    val serverVersion: String = "",
    val documentServerVersion: String = ""
)