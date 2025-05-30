package app.documents.core.utils

import android.text.Html
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.login.User

val User.displayNameFromHtml: String
    get() = Html.fromHtml(displayName, Html.FROM_HTML_MODE_LEGACY).toString()

fun User.toCloudAccount(portal: CloudPortal, socialProvider: String): CloudAccount {
    return CloudAccount(
        id = id,
        portalUrl = portal.url,
        login = email?.ifEmpty { userName }.orEmpty(),
        name = displayNameFromHtml,
        avatarUrl = avatarMedium,
        socialProvider = socialProvider,
        isAdmin = isAdmin,
        isVisitor = isVisitor,
        portal = portal
    )
}