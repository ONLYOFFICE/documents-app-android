package app.documents.shared

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import app.documents.shared.models.CommentMention
import app.documents.shared.models.MessengerMessage.GetCommentMentions
import app.documents.shared.utils.decodeFromString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json

class MessengerClient(
    lifecycleOwner: LifecycleOwner,
    private val context: Context
) {

    private val _eventFlows = mutableMapOf<Int, MutableSharedFlow<Any>>()

    private val responseHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            if (msg.what == GetCommentMentions.responseId) {
                val json = msg.data.getString(GetCommentMentions.responseKey)
                val users = Json.decodeFromString<List<CommentMention>>(json.orEmpty(), true)

                _eventFlows[GetCommentMentions.responseId]?.tryEmit(users.orEmpty())
            }
        }
    }

    private val replyMessenger = Messenger(responseHandler)

    private var serviceMessenger: Messenger? = null

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            serviceMessenger = Messenger(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
        }
    }

    init {
        val intent = Intent(context, MessengerService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        lifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    context.unbindService(connection)
                }
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun getCommentMentions(fileId: String): Flow<List<CommentMention>> {
        val flow = MutableSharedFlow<List<CommentMention>>(replay = 0, extraBufferCapacity = 1)
        val message = Message.obtain(null, GetCommentMentions.requestId)
        message.data = bundleOf(GetCommentMentions.responseKey to fileId)
        message.replyTo = replyMessenger
        serviceMessenger?.send(message)
        _eventFlows[GetCommentMentions.responseId] = flow as MutableSharedFlow<Any>
        return flow
    }
}