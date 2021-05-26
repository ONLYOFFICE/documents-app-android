package app.documents.core.network.models.login

import kotlinx.serialization.Serializable

@Serializable
data class Capabilities(
    val ldapEnabled: Boolean = false,
    val ssoUrl: String = "",
    val ssoLabel: String = "",
    val providers: List<String> = emptyList()
)