package app.documents.shared.models

internal sealed class MessengerMessage(
    val requestId: Int,
    val responseId: Int,
    val responseKey: String
) {

    object GetCommentUsers : MessengerMessage(0, 1, "get_comment_users_key")
}