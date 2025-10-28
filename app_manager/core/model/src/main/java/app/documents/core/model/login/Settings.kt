package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val communityServer: String? = null,
    val documentServer: String? = null,
    val mailServer: String? = null,
    val docSpace: String? = null
)