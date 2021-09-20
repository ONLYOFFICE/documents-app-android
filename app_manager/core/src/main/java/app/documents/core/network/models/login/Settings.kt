package app.documents.core.network.models.login

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val communityServer: String? = null,
    val documentServer: String? = null,
    val mailServer: String? = null
)