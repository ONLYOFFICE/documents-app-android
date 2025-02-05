package app.documents.core.network.share.models

import androidx.core.text.isDigitsOnly
import app.documents.core.model.cloud.Access
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ShareType {

    Admin, User, Group, Guests, Expected
}

@Serializable
data class Share(
    @SerialName("access") private val _access: String = Access.None.type,
    override val sharedTo: SharedTo = SharedTo(),
    val isLocked: Boolean = false,
    override val isOwner: Boolean = false,
    override val canEditAccess: Boolean = false,
    val subjectType: Int? = null,
) : ShareEntity {

    override val access: Access
        get() = if (_access.isDigitsOnly()) {
            Access.get(_access.toInt())
        } else {
            Access.get(_access)
        }

    companion object {

        private const val STATUS_EXPECT = 2
        private const val STATUS_GUEST = 1

        private const val SUBJECT_TYPE_GROUP = 2

        fun groupByAccess(shareList: List<Share>): Map<ShareType, List<Share>> {
            return shareList.groupBy { share ->
                val sharedTo = share.sharedTo
                when {
                    share.access == Access.RoomManager || share.isOwner -> ShareType.Admin
                    sharedTo.activationStatus == STATUS_EXPECT -> ShareType.Expected
                    sharedTo.activationStatus == STATUS_GUEST && sharedTo.isVisitor -> ShareType.Guests
                    share.subjectType == SUBJECT_TYPE_GROUP -> ShareType.Group
                    else -> ShareType.User
                }
            }
        }
    }
}