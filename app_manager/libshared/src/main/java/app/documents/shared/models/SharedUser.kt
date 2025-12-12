package app.documents.shared.models

import android.text.Html
import app.documents.core.model.login.User
import com.bumptech.glide.load.model.GlideUrl
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class SharedUser(
    val id: String,
    val displayName: String,
    val email: String = "",
    val avatarUrl: String? = null,
    @Contextual val avatarGlideUrl: GlideUrl? = null
) {

    fun filterByNameOrEmail(value: String): Boolean {
        val trimmedValue = value.trim()
        return displayName.contains(trimmedValue, false) || email.contains(trimmedValue, false)
    }

    companion object {

        fun from(user: User, portalUrl: String?): SharedUser {
            return SharedUser(
                id = user.id,
                displayName = Html
                    .fromHtml("${user.firstName} ${user.lastName}", Html.FROM_HTML_MODE_LEGACY)
                    .toString(),
                email = user.email.orEmpty(),
                avatarUrl = portalUrl?.let { "$portalUrl${user.avatarUrl}" }
            )
        }
    }
}