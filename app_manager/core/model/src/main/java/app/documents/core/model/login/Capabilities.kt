package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class Capabilities(
    val ldapEnabled: Boolean = false,
    val ssoUrl: String = "",
    val ssoLabel: String = "",
    val providers: List<String> = emptyList()
)