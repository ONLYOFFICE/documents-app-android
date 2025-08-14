package app.documents.core.model.login.response

data class AccessTokenResponse(
    val oauthToken: String,
    val oauthTokenSecret: String,
    val userId: String,
    val screenName: String
)