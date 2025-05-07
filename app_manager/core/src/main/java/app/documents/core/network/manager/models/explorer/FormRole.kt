package app.documents.core.network.manager.models.explorer

import app.documents.core.model.login.User
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class FormRole(

    @SerializedName("roleName")
    val roleName: String,

    @SerializedName("roleColor")
    val roleColor: String,

    @SerializedName("roleStatus")
    val roleStatus: Int,

    @SerializedName("sequence")
    val sequence: Int,

    @SerializedName("submitted")
    val submitted: Boolean,

    @SerializedName("user")
    val user: User,

    @SerializedName("history")
    val history: Map<String, String>? = null,

    @SerializedName("stopedBy")
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