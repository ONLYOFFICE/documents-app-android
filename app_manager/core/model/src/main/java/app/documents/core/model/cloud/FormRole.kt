package app.documents.core.model.cloud

import app.documents.core.model.login.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FormRole(

    @SerialName("roleName")
    val roleName: String,

    @SerialName("roleColor")
    val roleColor: String,

    @SerialName("roleStatus")
    val roleStatus: Int,

    @SerialName("sequence")
    val sequence: Int,

    @SerialName("submitted")
    val submitted: Boolean,

    @SerialName("user")
    val user: User,

    @SerialName("history")
    val history: Map<String, String>? = null,

    @SerialName("stoppedBy")
    val stoppedBy: User? = null
) {

    companion object {

        private val mock = FormRole(
            roleName = "Anyone",
            roleColor = "ffefbf",
            roleStatus = 2,
            sequence = 0,
            submitted = false,
            history = mapOf(),
            user = User(displayName = "Username")
        )

        val mockList: List<FormRole> = listOf(
            mock.copy(
                sequence = 1,
                roleStatus = 4,
                submitted = true,
                history = mapOf(
                    "2" to "2025-04-30T13:20:17+00:00",
                ),
                user = User(displayName = "Username"),
            ),
            mock.copy(
                sequence = 2,
                roleStatus = 2,
                submitted = false,

                ),
            mock.copy(
                sequence = 3,
                roleStatus = 1,
                submitted = false
            )
        )
    }
}