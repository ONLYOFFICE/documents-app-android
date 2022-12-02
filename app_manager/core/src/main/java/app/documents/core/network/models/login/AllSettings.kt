package app.documents.core.network.models.login

import kotlinx.serialization.Serializable

@Serializable
data class AllSettings(
    val personal: Boolean = false,
    val docSpace: Boolean = false
)