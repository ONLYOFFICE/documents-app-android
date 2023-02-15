package app.documents.core.network.storages.dropbox.models.request

import app.documents.core.network.storages.dropbox.models.operations.MoveCopyPaths
import kotlinx.serialization.Serializable

@Serializable
data class MoveCopyBatchRequest(
    val entries: List<MoveCopyPaths> = emptyList(),
    val autorename: Boolean = false,
    val allow_ownership_transfer: Boolean? = null
)
