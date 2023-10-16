package app.documents.core.network.storages.dropbox.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("account_id") var accountId: String? = null,
    @SerialName("email") var email: String? = null,
    @SerialName("name") var name: UserNameResponse? = null,
    @SerialName("profile_photo_url") var profilePhotoUrl: String? = null,
)