package app.documents.core.network.room.models

data class RequestFormRole(
    val roleColor: String,
    val roleName: String,
    val roomId: String,
    val userId: String
)

data class RequestFormRoleMapping(
    val formId: String,
    val roles: List<RequestFormRole>
)