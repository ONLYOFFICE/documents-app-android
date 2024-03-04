package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
data class PortalProvider(
    val provider: Provider? = null,
    // TODO: extract
    val webDavProvider: String = "",
    val webDavPath: String = "",
)