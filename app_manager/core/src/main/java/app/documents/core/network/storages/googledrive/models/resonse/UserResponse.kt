package app.documents.core.network.storages.googledrive.models.resonse

import app.documents.core.network.storages.googledrive.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val user: User? = null
)
