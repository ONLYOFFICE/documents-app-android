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
    sealed class Storage(var rootFolderId: String = "") : PortalProvider()

    @Serializable
    data object Onedrive : Storage()

    @Serializable
    data object Dropbox : Storage()

    @Serializable
    data object GoogleDrive : Storage()

    @Serializable
    data class Webdav(val provider: WebdavProvider, val path: String = "") : PortalProvider()

    companion object {

        val default: PortalProvider = Cloud.Workspace
    }
}