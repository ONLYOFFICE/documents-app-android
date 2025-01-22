package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
sealed class PortalProvider: java.io.Serializable {

    @Serializable
    sealed class Cloud : PortalProvider() {

        @Serializable
        data object Workspace : Cloud() {
            private fun readResolve(): Any = Workspace
        }

        @Serializable
        data object DocSpace : Cloud() {
            private fun readResolve(): Any = DocSpace
        }

        @Serializable
        data object Personal : Cloud() {
            private fun readResolve(): Any = Personal
        }
    }

    @Serializable
    sealed class Storage : PortalProvider()

    @Serializable
    data object Onedrive : Storage() {
        private fun readResolve(): Any = Onedrive
    }

    @Serializable
    data object Dropbox : Storage() {
        private fun readResolve(): Any = Dropbox
    }

    @Serializable
    data object GoogleDrive : Storage() {
        private fun readResolve(): Any = GoogleDrive
    }

    @Serializable
    data class Webdav(val provider: WebdavProvider, val path: String = "") : PortalProvider()

    companion object {

        val default: PortalProvider = Cloud.Workspace
    }
}