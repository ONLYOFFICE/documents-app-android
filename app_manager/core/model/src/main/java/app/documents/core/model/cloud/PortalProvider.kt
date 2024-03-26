package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
sealed class PortalProvider {

    @Serializable
    sealed class Cloud : PortalProvider() {

        @Serializable
        data object Workspace : Cloud()

        @Serializable
        data object DocSpace : Cloud()

        @Serializable
        data object Personal : Cloud()
    }

    @Serializable
    data object Onedrive : PortalProvider()

    @Serializable
    data object Dropbox : PortalProvider()

    @Serializable
    data object GoogleDrive : PortalProvider()

    @Serializable
    data class Webdav(val provider: WebdavProvider) : PortalProvider()

    companion object {

        val default: PortalProvider = Cloud.Workspace
    }
}