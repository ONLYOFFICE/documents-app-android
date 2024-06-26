package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
data class CloudPortal(
    val url: String = "",
    val scheme: Scheme = Scheme.Https,
    val socialProviders: List<String> = emptyList(),
    val provider: PortalProvider = PortalProvider.Cloud.Workspace,
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
        get() = url.endsWith(DOMAIN_INFO)

    val isPersonal: Boolean
        get() = url.contains("$PERSONAL_SUBDOMAIN$DEFAULT_HOST") ||
                url.contains("$PERSONAL_SUBDOMAIN$DEFAULT_HOST_INFO")

    val urlWithScheme: String
        get() = "${scheme.value}$url".let { if (!it.endsWith("/")) "$it/" else it }
}

val CloudPortal?.isDocSpace: Boolean
    get() = this?.provider is PortalProvider.Cloud.DocSpace