package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
data class CloudAccount(
    val id: String,
    val login: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val socialProvider: String = "",
    val isAdmin: Boolean = false,
    val isVisitor: Boolean = false,
    val portalUrl: String = "",
    val portal: CloudPortal = CloudPortal()
) {

    val isWebDav: Boolean
        get() = portal.provider is PortalProvider.Webdav

    val isGoogleDrive: Boolean
        get() = portal.provider == PortalProvider.GoogleDrive

    val isOneDrive: Boolean
        get() = portal.provider == PortalProvider.Onedrive

    val isDropbox: Boolean
        get() = portal.provider == PortalProvider.Dropbox

    val accountName: String
        get() = "$login@${portal.url}"

    fun isPersonal(): Boolean = portal.isPersonal
}

val CloudAccount?.isDocSpace: Boolean
    get() = this?.portal?.provider is PortalProvider.Cloud.DocSpace