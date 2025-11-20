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
import app.documents.core.providers.RoomProvider
import app.documents.shared.di.MessengerServiceApp
import app.documents.shared.models.CommentMention
import app.documents.shared.models.MessengerMessage.GetCommentMentions
import app.documents.shared.utils.encodeToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject


class MessengerService : Service() {

    @Inject
    lateinit var roomProvider: RoomProvider

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val requestHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(message: Message) {
            when (message.what) {
                GetCommentMentions.requestId -> replyCommentMentions(message)
            }
        }
    }

    private val app: MessengerServiceApp? by lazy { application as? MessengerServiceApp }
    private val messenger = Messenger(requestHandler)

    override fun onBind(intent: Intent): IBinder = messenger.binder

    override fun onCreate() {
        super.onCreate()
        val component = app?.createMessengerServiceComponent()
        component?.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        app?.destroyMessengerServiceComponent()
    }

    private fun replyCommentMentions(message: Message) {
        val fileId = message.data.getString(GetCommentMentions.responseKey)
        val replyTo = message.replyTo
        coroutineScope.launch {
            try {
                val users = roomProvider
                    .getUsersByItemId(requireNotNull(fileId) { "fileId is null" }, false)
                    .map(CommentMention::from)

                val replyMsg = Message
                    .obtain(null, GetCommentMentions.responseId)
                    .apply {
                        data = bundleOf(
                            GetCommentMentions.responseKey to
                                    Json.encodeToString(users, true)
                        )
                    }

                replyTo.send(replyMsg)
            } catch (e: Exception) {
                Log.e("MessengerService", e.message.toString())
            }
        }
    }
}