package app.documents.shared

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.core.os.bundleOf
import app.documents.shared.models.CommentUser
import app.documents.shared.models.MessengerMessage
import app.documents.shared.utils.encodeToString
import kotlinx.serialization.json.Json


class MessengerService : Service() {

    private val requestHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(message: Message) {
            try {
                when (message.what) {
                    MessengerMessage.GetCommentUsers.requestId -> {
                        val users = getCommentUsers()
                        val replyMsg = Message
                            .obtain(null, MessengerMessage.GetCommentUsers.responseId)
                            .apply {
                                data = bundleOf(
                                    MessengerMessage.GetCommentUsers.responseKey to
                                            Json.encodeToString(users, true)
                                )
                            }

                        message.replyTo.send(replyMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e("MessengerService", "can't handle message: $message, ${e.message}")
            }
        }
    }

    private val messenger = Messenger(requestHandler)

    override fun onBind(intent: Intent): IBinder = messenger.binder

    private fun getCommentUsers(): List<CommentUser> {
        return List(11) { CommentUser("", "userName", "email@emai.ru", "https://123") } // todo stub
    }
}