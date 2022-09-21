package app.documents.core.network.models.share.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestInviteLink(
    val emails: List<String>,
    val employeeType: Int,
    val access: Int
)
