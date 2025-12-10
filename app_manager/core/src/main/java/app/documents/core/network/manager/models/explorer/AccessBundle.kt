package app.documents.core.network.manager.models.explorer

import app.documents.core.model.cloud.Access
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


enum class AccessTarget {
    User, Group, ExternalLink, PrimaryLink;

    val isLink: Boolean
        get() = this == ExternalLink || this == PrimaryLink
}

@Serializable
data class AvailableShareRights(
    @SerializedName("User")
    val user: List<String> = emptyList(),

    @SerializedName("Group")
    val group: List<String> = emptyList(),

    @SerializedName("ExternalLink")
    val externalLink: List<String> = emptyList(),

    @SerializedName("PrimaryExternalLink")
    val primaryExternalLink: List<String> = emptyList()
) {

    fun toBundle(): AccessBundle {
        return AccessBundle.fromAvailable(this)
    }
}

@Serializable
data class AccessBundle(
    val user: List<Access> = emptyList(),
    val group: List<Access> = emptyList(),
    val externalLink: List<Access> = emptyList(),
    val primaryLink: List<Access> = emptyList(),
) {

    companion object {

        fun fromAvailable(rights: AvailableShareRights): AccessBundle {
            return AccessBundle(
                user = rights.user.mapNotNull(::mapAccess),
                group = rights.group.mapNotNull(::mapAccess),
                externalLink = rights.externalLink.mapNotNull(::mapAccess),
                primaryLink = rights.primaryExternalLink.mapNotNull(::mapAccess),
            )
        }

        private fun mapAccess(access: String): Access? {
            return when (access) {
                "ReadWrite" -> Access.ReadWrite
                "Editing" -> Access.Editor
                "Comment" -> Access.Comment
                "Review" -> Access.Review
                "FillForms" -> Access.FormFiller
                "Read" -> Access.Read
                "Restrict" -> Access.Restrict
                else -> null
            }
        }
    }

    fun fromTarget(target: AccessTarget): List<Access> {
        return when (target) {
            AccessTarget.User -> user
            AccessTarget.Group -> group
            AccessTarget.ExternalLink -> externalLink
            AccessTarget.PrimaryLink -> primaryLink
        }
    }
}