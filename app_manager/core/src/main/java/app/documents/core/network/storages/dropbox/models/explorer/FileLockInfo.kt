package app.documents.core.network.storages.dropbox.models.explorer

import kotlinx.serialization.Serializable


@Serializable
data class FileLockInfo(
    val is_lockholder: Boolean = false,
    val lockholder_name: String = "",
    val created: String = ""
)
