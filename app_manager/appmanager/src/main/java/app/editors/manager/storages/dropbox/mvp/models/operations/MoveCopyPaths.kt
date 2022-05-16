package app.editors.manager.storages.dropbox.mvp.models.operations

import kotlinx.serialization.Serializable

@Serializable
data class MoveCopyPaths(
    val from_path: String = "",
    val to_path: String = ""
)
