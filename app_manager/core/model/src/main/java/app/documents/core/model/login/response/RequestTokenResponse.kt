package app.documents.core.model.login.response

data class RequestTokenResponse(
    val oauthToken: String,
    val oauthTokenSecret: String,
    val callbackConfirmed: Boolean
)