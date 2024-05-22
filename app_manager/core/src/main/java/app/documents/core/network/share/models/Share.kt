package app.documents.core.network.share.models

import androidx.core.text.isDigitsOnly
import app.documents.core.network.common.contracts.ApiContract
import kotlinx.serialization.Serializable

enum class ShareType {

    Admin, User, Group, Expected
}

@Serializable
data class Share(
    val access: String = ApiContract.ShareType.NONE,
    override val sharedTo: SharedTo = SharedTo(),
    val isLocked: Boolean = false,
    override val isOwner: Boolean = false,
    override val canEditAccess: Boolean = false,
    val subjectType: Int? = null
) : ShareEntity {

    val intAccess: Int
        get() {
            return if (access.isDigitsOnly()) {
                access.toInt()
            } else {
                ApiContract.ShareType.getCode(access)
            }
        }

    override val accessCode: Int
        get() = intAccess

    companion object {

        fun groupByAccess(shareList: List<Share>): Map<ShareType, List<Share>> {
            return shareList.groupBy { share ->
                when {
                    arrayOf(
                        ApiContract.ShareCode.READ_WRITE,
                        ApiContract.ShareCode.ROOM_ADMIN
                    ).contains(share.intAccess) -> ShareType.Admin
                    share.sharedTo.activationStatus == 2 -> ShareType.Expected
                    share.subjectType == 2 -> ShareType.Group
                    else -> ShareType.User
                }
            }
        }
    }
}