package app.documents.core.network.storages.dropbox.models.response

import app.documents.core.network.storages.dropbox.models.user.Name
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
