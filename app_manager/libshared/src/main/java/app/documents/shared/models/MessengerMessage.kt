package app.documents.shared.models

internal sealed class MessengerMessage(
    val requestId: Int,
    val responseId: Int
) {

    object GetSharedUsers : MessengerMessage(0, 1) {
        const val FILE_ID_KEY = "file_id_key"
        const val RESPONSE_KEY = "get_users_response_key"
    }

    object GetAccessToken : MessengerMessage(1, 2) {
        const val RESPONSE_KEY = "get_access_token_key"
    }

    object SendMentionNotifications : MessengerMessage(3, 4) {
        const val FILE_ID_KEY = "file_id_key"
        const val EMAILS_KEY = "emails_key"
        const val COMMENT_KEY = "message_key"
    }
}