package app.documents.core.network.models.room

import kotlinx.serialization.Serializable

@Serializable
data class RequestSendLinks(
    val emails: List<String>,
    val employeeType: Int = 1,
    val access: Int = 1
)