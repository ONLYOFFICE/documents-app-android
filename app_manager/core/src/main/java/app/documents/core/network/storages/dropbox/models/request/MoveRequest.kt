package app.documents.core.network.storages.dropbox.models.request

import kotlinx.serialization.Serializable

@Serializable
data class MoveRequest(
    val from_path: String = "",
    val to_path: String = "",
    val allow_shared_folder: Boolean = false,
    val autorename: Boolean = false,
    val allow_ownership_transfer: Boolean = false
)
