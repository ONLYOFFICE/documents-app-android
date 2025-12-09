package app.documents.core.model.login.response

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.WebdavProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
data class OwnCloudUserResponse(
    val sub: String? = null,
    val name: String? = null,
    @SerialName("given_name")
    val givenName: String? = null,
    @SerialName("family_name")
    val familyName: String? = null,
    val email: String? = null,
    @SerialName("email_verified")
    val emailVerified: Boolean? = null,
    @SerialName("preferred_username")
    val preferredUsername: String? = null
) {
    fun toCloudAccount(url: URL): CloudAccount {
        val portal = url.toString().replace(".*://".toRegex(), "")
        return CloudAccount(
            id = requireNotNull(sub),
            portalUrl = portal,
            login = email.orEmpty(),
            name = preferredUsername.orEmpty(),
            portal = CloudPortal(
                url = portal,
                scheme = Scheme.Custom(url.protocol + "://"),
                provider = PortalProvider.Webdav(
                    provider = WebdavProvider.OwnCloud,
                    path = url.path.orEmpty() + WebdavProvider.DEFAULT_OWNCLOUD_PATH + "$preferredUsername/"
                )
            )
        )
    }
}