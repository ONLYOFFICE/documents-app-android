package app.documents.core.network.room.models

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.share.models.Share
import kotlinx.serialization.Serializable

@Serializable
data class ResponseRoomShare(val response: ResponseRoomMembers) : BaseResponse()

@Serializable
data class ResponseRoomMembers(val members: List<Share> = emptyList())