package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
data class CloudAccount(
    val id: String,
    val login: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = false,
    val isAdmin: Boolean = false,
    val isVisitor: Boolean = false,
    val portal: CloudPortal = CloudPortal()
) {

    val isWebDav: Boolean get() = portal.provider.provider == Provider.WEBDAV

    val isGoogleDrive: Boolean get() = portal.provider.provider == Provider.GOOGLE_DRIVE

    val isOneDrive: Boolean get() = portal.provider.provider == Provider.ONE_DRIVE

    val isDropbox: Boolean get() = portal.provider.provider == Provider.DROPBOX

    val accountName: String
        get() = "$login@${portal.portal}"

    fun isPersonal(): Boolean = portal.isPersonal
}