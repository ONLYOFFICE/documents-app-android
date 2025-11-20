package app.documents.shared.models

internal sealed class MessengerMessage(
    val requestId: Int,
    val responseId: Int,
    val responseKey: String
) {

    object GetCommentMentions : MessengerMessage(0, 1, "get_comment_mentions_key") {

        const val FILE_ID_KEY = "file_id_key"
    }
}