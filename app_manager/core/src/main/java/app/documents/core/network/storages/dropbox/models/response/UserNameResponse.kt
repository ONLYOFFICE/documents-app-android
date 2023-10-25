package app.documents.core.network.storages.dropbox.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserNameResponse(
    @SerialName("abbreviated_name") var abbreviatedName: String? = null,
    @SerialName("display_name") var displayName: String? = null,
    @SerialName("familiar_name") var familiarName: String? = null,
    @SerialName("given_name") var givenName: String? = null,
    @SerialName("surname") var surname: String? = null
)