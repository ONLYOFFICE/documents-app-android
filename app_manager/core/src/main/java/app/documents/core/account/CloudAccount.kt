package app.documents.core.account

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.documents.core.network.ApiContract
import kotlinx.serialization.Serializable
import lib.toolkit.base.managers.utils.CryptUtils

@Entity
@Serializable
data class CloudAccount(
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
    val refreshToken: String = ""
) {

    var token: String = ""

    var password: String = ""

    var expires: String = ""

    fun getAccountName() = "$login@$portal"

    fun isPersonal() = portal?.contains(ApiContract.PERSONAL_HOST) == true || portal?.contains(ApiContract.PERSONAL_INFO_HOST) == true

    fun setCryptToken(value: String) {
        token = CryptUtils.encryptAES128(value, id) ?: ""
    }

    fun setCryptPassword(value: String) {
        password = CryptUtils.encryptAES128(value, id) ?: ""
    }

    fun getDecryptToken() = CryptUtils.decryptAES128(token, id)

    fun getDecryptPassword() = CryptUtils.decryptAES128(password, id)

}

fun CloudAccount.copyWithToken(
    id: String = this.id,
    login: String? = this.login,
    portal: String? = this.portal,
    serverVersion: String = this.serverVersion,
    scheme: String? = this.scheme,
    name: String? = this.name,
    provider: String? = this.provider,
    avatarUrl: String? = this.avatarUrl,
    isSslCiphers: Boolean = this.isSslCiphers,
    isSslState: Boolean = this.isSslState,
    isOnline: Boolean = this.isOnline,
    isWebDav: Boolean = this.isWebDav,
    isOneDrive: Boolean = this.isOneDrive,
    isDropbox: Boolean = this.isDropbox,
    isGoogleDrive: Boolean = this.isGoogleDrive,
    webDavProvider: String? = this.webDavProvider,
    webDavPath: String? = this.webDavPath,
    isAdmin: Boolean = this.isAdmin,
    isVisitor: Boolean = this.isVisitor
): CloudAccount {
    return CloudAccount(
        id = id,
        login = login,
        portal = portal,
        serverVersion = serverVersion,
        scheme = scheme,
        name = name,
        provider = provider,
        avatarUrl = avatarUrl,
        isSslCiphers = isSslCiphers,
        isSslState = isSslState,
        isOnline = isOnline,
        isWebDav = isWebDav,
        isOneDrive = isOneDrive,
        isDropbox = isDropbox,
        isGoogleDrive = isGoogleDrive,
        webDavProvider = webDavProvider,
        webDavPath = webDavPath,
        isAdmin = isAdmin,
        isVisitor = isVisitor
    ).apply {
        token = this@copyWithToken.token
        password = this@copyWithToken.password
        expires = this@copyWithToken.expires
    }
}