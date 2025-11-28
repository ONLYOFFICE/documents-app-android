package app.documents.shared.models

internal sealed class MessengerMessage(
    val requestId: Int,
    val responseId: Int
) {

    object GetCommentMentions : MessengerMessage(0, 1) {

        const val FILE_ID_KEY = "file_id_key"
        const val FILTER_VALUE_KEY = "filter_value_key"
        const val COMMENT_MENTIONS_KEY = "comment_mentions_key"
    }

    object GetAvatarUrls : MessengerMessage(2, 3) {

        const val USER_IDS_KEY = "user_ids_key"
        const val RESPONSE_KEY = "response_key"
    }
}