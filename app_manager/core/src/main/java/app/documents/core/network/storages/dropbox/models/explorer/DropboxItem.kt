package app.documents.core.network.storages.dropbox.models.explorer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DropboxItem(
    @SerialName(".tag") val tag: String = "",
    val name: String = "",
    val id: String = "",
    val client_modified: String = "",
    val server_modified: String = "",
    val rev: String = "",
    val size: String = "",
    val path_lower: String = "",
    val path_display: String = "",
    val sharing_info: SharingInfo? = null,
    val is_downloadable: Boolean = false,
    val property_groups: List<PropertyGroup>? = emptyList(),
    val has_explicit_shared_members: Boolean = false,
    val content_hash: String = "",
    val file_lock_info: FileLockInfo? = null
)
