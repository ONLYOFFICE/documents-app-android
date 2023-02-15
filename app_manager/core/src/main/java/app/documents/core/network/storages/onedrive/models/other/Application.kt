package app.documents.core.network.storages.onedrive.models.other

import kotlinx.serialization.Serializable


@Serializable
data class Application(
    val id: String = "",
    val displayName: String = ""
)
