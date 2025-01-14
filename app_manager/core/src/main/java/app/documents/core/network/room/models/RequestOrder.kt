package app.documents.core.network.room.models

import app.documents.core.model.cloud.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestOrder(

    @SerialName("items")
    val items: List<Order>
)