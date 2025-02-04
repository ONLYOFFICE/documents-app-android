package app.documents.core.network.share.models

import app.documents.core.model.cloud.Access
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ShareType {

    Admin, User, Group, Expected
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
    val subjectType: Int? = null
) : ShareEntity {

    override val access: Access
        get() = Access.get(_access.toInt())

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