package app.documents.core.network.manager.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseRoomNotifications(

    @SerialName("disabledRooms")
    val disabledRooms: List<String>
)