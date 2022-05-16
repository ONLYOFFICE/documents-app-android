package app.editors.manager.storages.googledrive.mvp.models.resonse

import app.editors.manager.storages.googledrive.mvp.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val user: User? = null
)
