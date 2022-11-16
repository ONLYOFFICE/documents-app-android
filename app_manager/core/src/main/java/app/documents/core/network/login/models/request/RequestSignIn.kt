package app.documents.core.network.login.models.request

import kotlinx.serialization.Serializable

@Serializable
open class RequestSignIn(val userName: String = "",
                         val password: String = "",
                         val provider: String = "",
                         val accessToken: String = "")