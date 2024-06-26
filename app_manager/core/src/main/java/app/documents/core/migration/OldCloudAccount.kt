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

data class CloudAccountWithTokenAndPassword(
    val token: String?,
    val password: String?,
    val cloudAccount: CloudAccount
)

@Entity(tableName = "CloudAccount")
@Serializable
data class OldCloudAccount(
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


fun OldCloudAccount.toCloudAccountWithTokenAndPassword():
        CloudAccountWithTokenAndPassword {

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
            portal = CloudPortal(
                url = portal.orEmpty(),
                scheme = Scheme.valueOf(scheme.orEmpty()),
                version = PortalVersion(serverVersion = serverVersion),
                settings = PortalSettings(isSslState = isSslState, isSslCiphers = isSslCiphers),
                provider = when {
                    isPersonal -> PortalProvider.Cloud.Personal
                    isWebDav -> PortalProvider.Webdav(WebdavProvider.valueOf(webDavProvider.orEmpty()), webDavPath.orEmpty())
                    isOneDrive -> PortalProvider.Onedrive
                    isGoogleDrive -> PortalProvider.GoogleDrive
                    isDropbox -> PortalProvider.Dropbox
                    else -> PortalProvider.default
                }
            )
        ),
        token = getDecryptToken(),
        password = getDecryptPassword()
    )
}

internal fun OldCloudAccount.toCloudAccountWithTokenAndPassword(networkSettings: NetworkSettings? = null):
        CloudAccountWithTokenAndPassword {

    val accountWithTokenAndPassword = toCloudAccountWithTokenAndPassword()
    val cloudPortal = accountWithTokenAndPassword.cloudAccount.portal

    return accountWithTokenAndPassword.copy(
        cloudAccount = accountWithTokenAndPassword.cloudAccount.copy(
            portal = if (networkSettings != null && isOnline) {
                with(networkSettings) {
                    cloudPortal.copy(
                        provider = if (isDocSpace) PortalProvider.Cloud.DocSpace else cloudPortal.provider,
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
            } else cloudPortal
        )
    )
}