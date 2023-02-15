package app.documents.core.network.storages.googledrive.models.resonse

import kotlinx.serialization.Serializable

@Serializable
data class FileCapabilities(
    val canDelete: Boolean = true
)
