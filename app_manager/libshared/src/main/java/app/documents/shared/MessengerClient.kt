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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import app.documents.shared.models.CommentUser
import app.documents.shared.models.MessengerMessage
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
            if (msg.what == MessengerMessage.GetCommentUsers.responseId) {
                val json = msg.data.getString(MessengerMessage.GetCommentUsers.responseKey)
                val users = json?.let {
                    Json.decodeFromString<List<CommentUser>>(string = it, decodeUrl = true)
                }.orEmpty()

                _eventFlows[MessengerMessage.GetCommentUsers.responseId]?.tryEmit(users)
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
    fun getCommentUsers(): Flow<List<CommentUser>> {
        val message = Message.obtain(null, MessengerMessage.GetCommentUsers.requestId)
        val flow = MutableSharedFlow<List<CommentUser>>(replay = 0, extraBufferCapacity = 1)
        message.replyTo = replyMessenger
        serviceMessenger?.send(message)
        _eventFlows[MessengerMessage.GetCommentUsers.responseId] = flow as MutableSharedFlow<Any>
        return flow
    }
}