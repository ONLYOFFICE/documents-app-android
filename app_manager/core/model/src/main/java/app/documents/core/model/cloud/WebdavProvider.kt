package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
sealed class WebdavProvider(val path: String, val name: String) : java.io.Serializable {

    data class NextCloud(val defaultPath: String = DEFAULT_NEXT_CLOUD_PATH) :
        WebdavProvider(defaultPath, NAME_NEXTCLOUD)

    data object OwnCloud : WebdavProvider("/remote.php/dav/files/", NAME_OWNCLOUD)
    data object KDrive : WebdavProvider("/", NAME_K_DRIVE)
    data object Yandex : WebdavProvider("/", NAME_YANDEX)
    data object WebDav : WebdavProvider("/", NAME_WEBDAV)

    companion object {

        const val DEFAULT_NEXT_CLOUD_PATH = "/remote.php/dav/files/"

        private const val NAME_NEXTCLOUD = "NextCloud"
        private const val NAME_OWNCLOUD = "OwnCloud"
        private const val NAME_YANDEX = "Yandex"
        private const val NAME_K_DRIVE = "KDrive"
        private const val NAME_WEBDAV = "WebDav"

        fun valueOf(portalProvider: PortalProvider): WebdavProvider {
            return (portalProvider as? PortalProvider.Webdav)?.provider
                ?: throw IllegalArgumentException("$portalProvider is not a webdav provider")
        }
    }
}