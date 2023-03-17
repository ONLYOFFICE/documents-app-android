package app.documents.core.network.login.models

import kotlinx.serialization.Serializable

@Serializable
data class AllSettings(
    val personal: Boolean = false,
    val docSpace: Boolean = false
)