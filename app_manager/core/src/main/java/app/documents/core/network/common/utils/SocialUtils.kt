package app.documents.core.network.common.utils

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.BuildConfig

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
    open val state: String = BuildConfig.SOCIALS_STATE
) {

    data object Twitter : SocialSignIn(
        providerKey = ApiContract.Social.TWITTER,
        authUrl = "https://api.x.com/oauth/authenticate",
        clientId = BuildConfig.TWITTER_CLIENT_ID,
        scope = "users.read users.email offline.access"
    ) {
        override val state = BuildConfig.TWITTER_OAUTH1_STATE
        const val SECRET_KEY = BuildConfig.TWITTER_SECRET_KEY
        val callbackUrl = "$redirectUri?state=$state"
    }

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