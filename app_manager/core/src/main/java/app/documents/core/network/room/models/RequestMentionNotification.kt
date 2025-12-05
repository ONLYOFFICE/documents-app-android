package app.documents.core.network.room.models

data class RequestMentionNotification(
    val emails: List<String>,
    val message: String
)