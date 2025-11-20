package app.documents.shared.models

import app.documents.core.model.login.User
import app.documents.core.utils.displayNameFromHtml
import kotlinx.serialization.Serializable

@Serializable
data class CommentMention(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String
) {

    companion object {

        fun from(user: User): CommentMention {
            return CommentMention(
                id = user.id,
                displayName = user.displayNameFromHtml,
                email = user.email.orEmpty(),
                avatarUrl = user.avatarUrl
            )
        }
    }
}