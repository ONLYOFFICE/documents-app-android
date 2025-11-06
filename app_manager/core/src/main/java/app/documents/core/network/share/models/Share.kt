package app.documents.core.network.share.models

import app.documents.core.model.cloud.Access
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ShareType {

    Admin, User, Group, Guests, Expected, Owner
}

@Serializable
data class Share(
    @SerialName("access")
    @SerializedName("access")
    private val _access: Int = Access.None.code,

    override val sharedTo: SharedTo = SharedTo(),
    val isLocked: Boolean = false,
    override val isOwner: Boolean = false,
    override val canEditAccess: Boolean = false,
    val subjectType: Int? = null,
) : ShareEntity {

    companion object {

        private const val STATUS_EXPECT = 2
        private const val STATUS_GUEST = 1

        private const val SUBJECT_TYPE_GROUP = 2
    }

    override val access: Access
        get() = Access.get(_access)

    val roomAccessType: ShareType
        get() = when {
            access == Access.RoomManager || isOwner -> ShareType.Admin
            isExpected -> ShareType.Expected
            isGuest -> ShareType.Guests
            isGroup -> ShareType.Group
            else -> ShareType.User
        }

    val itemAccessType: ShareType
        get() = when {
            isOwner -> ShareType.Owner
            isGroup -> ShareType.Group
            else -> ShareType.User
        }

    val isGroup: Boolean
        get() = subjectType == SUBJECT_TYPE_GROUP

    private val isGuest: Boolean
        get() = sharedTo.activationStatus == STATUS_GUEST && sharedTo.isVisitor

    private val isExpected: Boolean
        get() = sharedTo.activationStatus == STATUS_EXPECT

}