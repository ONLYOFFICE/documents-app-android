package app.documents.core.network.manager.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestRoomNotifications(

    @SerialName("RoomsId")
    val roomsId: Int,

    @SerialName("Mute")
    val mute: Boolean
)