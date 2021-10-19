package app.editors.manager.dropbox.mvp.models.explorer

import kotlinx.serialization.Serializable


@Serializable
data class SharingInfo(
    val read_only: Boolean = false,
    val parent_shared_folder_id: String = "",
    val modifed_by: String = ""
)
