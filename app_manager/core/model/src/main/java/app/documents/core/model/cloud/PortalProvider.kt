package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
sealed class PortalProvider {

    data object Cloud : PortalProvider()
    data object DocSpace : PortalProvider()
    data object OneDrive : PortalProvider()
    data object DropBox : PortalProvider()
    data object GoogleDrive : PortalProvider()
    data class Webdav(val provider: WebdavProvider) : PortalProvider()
}