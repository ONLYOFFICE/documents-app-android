package app.documents.core.account

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import app.documents.core.network.ApiContract
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    val webDavProvider: String? = null,
    val webDavPath: String? = null,
    val isAdmin: Boolean = false,
    val isVisitor: Boolean = false
) {

    @Transient
    @Ignore
    var token: String? = null

    fun getAccountName() = "$login@$portal"

    fun isPersonal() = portal?.contains(ApiContract.PERSONAL_SUBDOMAIN) ?: false

}