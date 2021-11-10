package app.editors.manager.dropbox.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ExplorerRequest(
    val path: String = "",
    val recursive: Boolean = false,
    val include_media_info: Boolean = false,
    val include_deleted: Boolean = false,
    val include_has_explicit_shared_members: Boolean = false,
    val include_mounted_folders: Boolean = true,
    val include_non_downloadable_files: Boolean = true
)
