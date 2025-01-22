package app.documents.core.network.share.models

import app.documents.core.model.cloud.Access
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GroupShare(
    override val canEditAccess: Boolean = false,
    @SerializedName("owner") override val isOwner: Boolean = false,
    @SerializedName("user") override val sharedTo: SharedTo = SharedTo(),
    val groupAccess: Int = 0,
    val userAccess: Int? = null,
    val overridden: Boolean = false,
) : ShareEntity {

    override val access: Access
        get() {
            val access = userAccess ?: groupAccess
            return Access.get(access)
        }
}