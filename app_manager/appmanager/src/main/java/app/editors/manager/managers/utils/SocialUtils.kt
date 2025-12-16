package app.editors.manager.managers.utils

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.BuildConfig
import app.documents.core.network.common.contracts.StorageContract
import java.util.TreeMap
import kotlin.collections.set

object SocialUtils {
    fun getSocialSignIn(providerKey: String?): SocialSignIn? {
        return when (providerKey) {
            ApiContract.Social.TWITTER -> SocialSignIn.Twitter
            ApiContract.Social.ZOOM -> SocialSignIn.Zoom
            ApiContract.Social.LINKEDIN -> SocialSignIn.LinkedIn
            ApiContract.Social.APPLE_ID -> SocialSignIn.AppleId
            else -> null
        }
    }
}

sealed class SocialSignIn(
    val providerKey: String,
    val authUrl: String,
    val clientId: String,
    val scope: String?,
    val redirectUri: String = BuildConfig.SOCIALS_REDIRECT_URL,
    val state: String = BuildConfig.SOCIALS_STATE
) {

    fun getUrl(): String {
        val uriMap = TreeMap<String, String>()
        uriMap[StorageContract.ARG_RESPONSE_TYPE] = StorageContract.ARG_CODE
        uriMap[StorageContract.ARG_CLIENT_ID] = clientId
        uriMap[StorageContract.ARG_REDIRECT_URI] = redirectUri
        uriMap[StorageContract.ARG_STATE] = state
        scope?.let { uriMap[StorageContract.ARG_SCOPE] = scope }

        return StorageUtils.getUrl(authUrl, uriMap)
    }

    data object Twitter : SocialSignIn(
        providerKey = ApiContract.Social.TWITTER,
        authUrl = "https://api.x.com/oauth/authenticate",
        clientId = BuildConfig.TWITTER_CLIENT_ID,
        scope = "users.read users.email offline.access"
    )

    data object Zoom : SocialSignIn(
        providerKey = ApiContract.Social.ZOOM,
        authUrl = "https://zoom.us/oauth/authorize?",
        clientId = BuildConfig.ZOOM_CLIENT_ID,
        scope = null
    )

    data object LinkedIn : SocialSignIn(
        providerKey = ApiContract.Social.LINKEDIN,
        authUrl = "https://www.linkedin.com/oauth/v2/authorization?",
        clientId = BuildConfig.LINKEDIN_CLIENT_ID,
        scope = "r_liteprofile r_emailaddress"
    )

    data object AppleId : SocialSignIn(
        providerKey = ApiContract.Social.APPLE_ID,
        authUrl = "https://appleid.apple.com/auth/authorize?",
        clientId = BuildConfig.APPLE_CLIENT_ID,
        scope = null
    )
}