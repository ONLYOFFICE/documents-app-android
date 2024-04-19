package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateThirdPartyRoom(
    val roomType: Int,
    val createAsNewFolder: Boolean,
    val title: String
)