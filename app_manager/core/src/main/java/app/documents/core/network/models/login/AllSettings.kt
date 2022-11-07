package app.documents.core.network.models.login

import app.documents.core.network.models.Base
import kotlinx.serialization.Serializable

@Serializable
data class AllSettings(
    val personal: Boolean = false,
    val docSpace: Boolean = false
)