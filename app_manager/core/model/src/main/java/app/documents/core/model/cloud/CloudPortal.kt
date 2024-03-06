package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
data class CloudPortal(
    val portalId: String = "",
    val accountId: String = "",
    val scheme: Scheme = Scheme.Https,
    val portal: String = "",
    val provider: PortalProvider = PortalProvider.Cloud,
    val version: PortalVersion = PortalVersion(),
    val settings: PortalSettings = PortalSettings()
) {

    companion object {

        const val DOMAIN_INFO = ".info"
        const val PERSONAL_SUBDOMAIN = "personal"
        const val DEFAULT_HOST = ".onlyoffice.com"
        const val DEFAULT_HOST_INFO = ".teamlab.info"
    }

    val isPortalInfo: Boolean
        get() = portal.endsWith(DOMAIN_INFO)

    val isPersonal: Boolean
        get() = portal.contains("$PERSONAL_SUBDOMAIN$DEFAULT_HOST") ||
                portal.contains("$PERSONAL_SUBDOMAIN$DEFAULT_HOST_INFO")

    val url: String
        get() = "$scheme$portal"
}