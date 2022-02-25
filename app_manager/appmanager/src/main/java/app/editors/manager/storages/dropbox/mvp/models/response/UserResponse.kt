package app.editors.manager.storages.dropbox.mvp.models.response

import app.editors.manager.storages.dropbox.mvp.models.user.Name
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val account_id: String = "",
    val name: Name? = null,
    val email: String = "",
    val email_verified: Boolean = false,
    val disabled: Boolean = false,
    val is_teammate: Boolean = false,
    val profile_photo_url: String = "",
    val team_member_id: String? = null
)
