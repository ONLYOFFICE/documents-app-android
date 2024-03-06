package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
sealed class PortalProvider {

    @Serializable
    data object Cloud : PortalProvider()

    @Serializable
    data object DocSpace : PortalProvider()

    @Serializable
    data object OneDrive : PortalProvider()

    @Serializable
    data object DropBox : PortalProvider()

    @Serializable
    data object GoogleDrive : PortalProvider()

    @Serializable
    data class Webdav(val provider: WebdavProvider) : PortalProvider()
}