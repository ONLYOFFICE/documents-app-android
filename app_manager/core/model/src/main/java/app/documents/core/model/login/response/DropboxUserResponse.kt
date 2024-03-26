package app.documents.core.model.login.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DropboxUserResponse(
    @SerialName("account_id") var accountId: String? = null,
    @SerialName("email") var email: String? = null,
    @SerialName("name") var name: DropboxUserNameResponse? = null,
    @SerialName("profile_photo_url") var profilePhotoUrl: String? = null,
)

@Serializable
data class DropboxUserNameResponse(
    @SerialName("abbreviated_name") var abbreviatedName: String? = null,
    @SerialName("display_name") var displayName: String? = null,
    @SerialName("familiar_name") var familiarName: String? = null,
    @SerialName("given_name") var givenName: String? = null,
    @SerialName("surname") var surname: String? = null
)
