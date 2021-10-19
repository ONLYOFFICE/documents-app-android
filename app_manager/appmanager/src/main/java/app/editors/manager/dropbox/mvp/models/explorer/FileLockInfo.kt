package app.editors.manager.dropbox.mvp.models.explorer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class FileLockInfo(
    val is_lockholder: Boolean = false,
    val lockholder_name: String = "",
    val created: String = ""
)
