package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
@Serializable
sealed class WebdavProvider(val name: String) : java.io.Serializable {

    @Serializable
    data object NextCloud : WebdavProvider(NAME_NEXTCLOUD)

    @Serializable
    data object OwnCloud : WebdavProvider(NAME_OWNCLOUD)

    @Serializable
    data object KDrive : WebdavProvider(NAME_K_DRIVE)

    @Serializable
    data object Yandex : WebdavProvider(NAME_YANDEX)

    @Serializable
    data object WebDav : WebdavProvider(NAME_WEBDAV)

    companion object {

        const val DEFAULT_NEXT_CLOUD_PATH = "/remote.php/dav/files/"
        const val DEFAULT_OWNCLOUD_PATH = "/remote.php/dav/files/"

        private const val NAME_NEXTCLOUD = "NextCloud"
        private const val NAME_OWNCLOUD = "OwnCloud"
        private const val NAME_YANDEX = "Yandex"
        private const val NAME_K_DRIVE = "KDrive"
        private const val NAME_WEBDAV = "WebDav"

        fun valueOf(portalProvider: PortalProvider): WebdavProvider {
            return (portalProvider as? PortalProvider.Webdav)?.provider
                ?: throw IllegalArgumentException("$portalProvider is not a webdav provider")
        }

        fun valueOf(provider: String): WebdavProvider {
            return when (provider) {
                NAME_NEXTCLOUD -> NextCloud
                NAME_OWNCLOUD -> OwnCloud
                NAME_YANDEX -> Yandex
                NAME_K_DRIVE -> KDrive
                NAME_WEBDAV -> WebDav
                else -> throw IllegalArgumentException("$provider is not a webdav provider")
            }
        }
    }
}