package app.documents.core.network.share.models

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

    override val accessCode: Int
        get() = userAccess ?: groupAccess
}