package app.documents.core.network.login.models

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val communityServer: String? = null,
    val documentServer: String? = null,
    val mailServer: String? = null
)