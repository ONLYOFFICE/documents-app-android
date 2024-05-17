package app.documents.core.network.share.models

import androidx.core.text.isDigitsOnly
import app.documents.core.network.common.contracts.ApiContract
import kotlinx.serialization.Serializable

enum class ShareGroup {

    Admin, User, Group, Expected
}

@Serializable
data class Share(
    val access: String = ApiContract.ShareType.NONE,
    val sharedTo: SharedTo = SharedTo(),
    val isLocked: Boolean = false,
    val isOwner: Boolean = false,
    val canEditAccess: Boolean = false,
    val subjectType: Int? = null
) {
    val intAccess: Int
        get() {
            return if (access.isDigitsOnly()) {
                access.toInt()
            } else {
                ApiContract.ShareType.getCode(access)
            }
        }

    companion object {

        fun groupByAccess(shareList: List<Share>): Map<ShareGroup, List<Share>> {
            return shareList.groupBy { share ->
                when {
                    arrayOf(
                        ApiContract.ShareCode.READ_WRITE,
                        ApiContract.ShareCode.ROOM_ADMIN
                    ).contains(share.intAccess) -> ShareGroup.Admin
                    share.sharedTo.activationStatus == 2 -> ShareGroup.Expected
                    share.subjectType == 2 -> ShareGroup.Group
                    else -> ShareGroup.User
                }
            }
        }
    }
}