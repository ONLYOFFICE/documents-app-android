package app.documents.core.model.login.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnedriveUser(
    @SerialName("@odata.context") val context: String = "",
    val displayName: String = "",
    val surname: String = "",
    val givenName: String = "",
    val id: String = "",
    val userPrincipalName: String = "",
    val businessPhones: List<String> = emptyList(),
    val jobTitle: String? = null,
    val mail: String? = null,
    val mobilePhone: String? = null,
    val officeLocation: String? = null,
    val preferredLanguage: String? = null
)