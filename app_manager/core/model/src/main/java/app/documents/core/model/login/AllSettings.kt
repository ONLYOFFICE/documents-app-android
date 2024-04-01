package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class AllSettings(
    val personal: Boolean = false,
    val docSpace: Boolean = false
)