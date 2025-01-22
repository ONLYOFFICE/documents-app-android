package app.documents.core.network.share.models

import androidx.core.text.isDigitsOnly
import app.documents.core.model.cloud.Access
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ShareType {

    Admin, User, Group, Expected
}

@Serializable
data class Share(
    @SerialName("access") private val _access: String = Access.None.type,
    override val sharedTo: SharedTo = SharedTo(),
    val isLocked: Boolean = false,
    override val isOwner: Boolean = false,
    override val canEditAccess: Boolean = false,
    val subjectType: Int? = null
) : ShareEntity {

    override val access: Access
        get() = if (_access.isDigitsOnly()) {
            Access.get(_access.toInt())
        } else {
            Access.get(_access)
        }

    companion object {

        fun groupByAccess(shareList: List<Share>): Map<ShareType, List<Share>> {
            return shareList.groupBy { share ->
                when {
                    share.access == Access.RoomManager -> ShareType.Admin
                    share.sharedTo.activationStatus == 2 -> ShareType.Expected
                    share.subjectType == 2 -> ShareType.Group
                    else -> ShareType.User
                }
            }
        }
    }
}