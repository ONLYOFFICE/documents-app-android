package app.editors.manager.storages.dropbox.mvp.models.request

import app.editors.manager.storages.dropbox.mvp.models.operations.MoveCopyPaths
import kotlinx.serialization.Serializable

@Serializable
data class MoveCopyBatchRequest(
    val entries: List<MoveCopyPaths> = emptyList(),
    val autorename: Boolean = false,
    val allow_ownership_transfer: Boolean? = null
)
