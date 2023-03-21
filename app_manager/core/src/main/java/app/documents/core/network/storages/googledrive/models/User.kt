package app.documents.core.network.storages.googledrive.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val kind: String = "",
    val displayName: String = "",
    val photoLink: String = "",
    val me: Boolean = false,
    val permissionId: String = "",
    val emailAddress: String = ""
)
