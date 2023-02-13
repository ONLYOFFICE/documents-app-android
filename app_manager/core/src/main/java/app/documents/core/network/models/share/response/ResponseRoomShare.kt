package app.documents.core.network.models.share.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.share.Share
import kotlinx.serialization.Serializable

@Serializable
data class ResponseRoomShare(val response: ResponseRoomMembers) : Base()

@Serializable
data class ResponseRoomMembers(val members: List<Share> = emptyList())