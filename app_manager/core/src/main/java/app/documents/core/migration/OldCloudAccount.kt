package app.documents.core.migration

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.contracts.ApiContract
import kotlinx.serialization.Serializable
import lib.toolkit.base.managers.utils.CryptUtils

internal data class CloudAccountWithTokenAndPassword(
    val token: String?,
    val password: String?,
    val cloudAccount: CloudAccount,
    val online: Boolean
)

@Entity(tableName = "CloudAccount")
@Serializable
internal data class OldCloudAccount(
    @PrimaryKey
    val id: String,
    val login: String? = null,
    val portal: String? = null,
    val serverVersion: String = "",
    val scheme: String? = null,
    val name: String? = null,
    val provider: String? = null,
    val avatarUrl: String? = null,
    val isSslCiphers: Boolean = false,
    val isSslState: Boolean = true,
    val isOnline: Boolean = false,
    val isWebDav: Boolean = false,
    val isOneDrive: Boolean = false,
    val isDropbox: Boolean = false,
    val isGoogleDrive: Boolean = false,
    val webDavProvider: String? = null,
    val webDavPath: String? = null,
    val isAdmin: Boolean = false,
    val isVisitor: Boolean = false,
    val refreshToken: String = "",
    val token: String = "",
    val password: String = "",
    val expires: String = ""
) {

    val isPersonal: Boolean
        get() = portal?.contains(ApiContract.PERSONAL_HOST) == true ||
                portal?.contains(ApiContract.PERSONAL_INFO_HOST) == true

    fun getDecryptToken() = CryptUtils.decryptAES128(token, id)

    fun getDecryptPassword() = CryptUtils.decryptAES128(password, id)
}

internal fun OldCloudAccount.toCloudAccountWithTokenAndPassword(networkSettings: NetworkSettings):
        CloudAccountWithTokenAndPassword {

    var cloudPortal = CloudPortal(
        url = portal.orEmpty(),
        scheme = Scheme.valueOf(scheme.orEmpty()),
        version = PortalVersion(serverVersion = serverVersion),
        settings = PortalSettings(isSslState = isSslState, isSslCiphers = isSslCiphers),
        provider = when {
            isPersonal -> PortalProvider.Cloud.Personal
            isWebDav -> PortalProvider.Webdav(WebdavProvider.valueOf(webDavProvider.orEmpty()))
            isOneDrive -> PortalProvider.Onedrive
            isGoogleDrive -> PortalProvider.GoogleDrive
            isDropbox -> PortalProvider.Dropbox
            else -> PortalProvider.default
        }
    )

    if (isOnline) {
        with(networkSettings) {
            when {
                isDocSpace -> cloudPortal = cloudPortal.copy(provider = PortalProvider.Cloud.DocSpace)
            }
            cloudPortal = cloudPortal.copy(
                version = cloudPortal.version.copy(
                    serverVersion = serverVersion,
                    documentServerVersion = documentServerVersion
                ),
                settings = cloudPortal.settings.copy(
                    ssoLabel = ssoLabel,
                    ssoUrl = ssoUrl,
                    ldap = ldap
                )
            )
        }
    }

    return CloudAccountWithTokenAndPassword(
        cloudAccount = CloudAccount(
            id = id,
            login = login.orEmpty(),
            name = name.orEmpty(),
            avatarUrl = avatarUrl.orEmpty(),
            socialProvider = provider.orEmpty(),
            isAdmin = isAdmin,
            isVisitor = isVisitor,
            portalUrl = portal.orEmpty(),
            portal = cloudPortal
        ),
        token = getDecryptToken(),
        password = getDecryptPassword(),
        online = isOnline
    )
}