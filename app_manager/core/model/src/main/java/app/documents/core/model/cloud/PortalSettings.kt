package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
data class PortalSettings(
    val isSslState: Boolean = true,
    val isSslCiphers: Boolean = false,
    val ssoUrl: String = "",
    val ssoLabel: String = "",
    val ldap: Boolean = false
)