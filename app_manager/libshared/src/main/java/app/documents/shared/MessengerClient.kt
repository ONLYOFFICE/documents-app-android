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
import app.documents.shared.models.MessengerMessage.GetAvatarUrls
import app.documents.shared.models.MessengerMessage.GetCommentMentions
import app.documents.shared.utils.decodeFromString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json

class MessengerClient(private val context: Context) {

    companion object {

        private const val CONNECTION_TIMEOUT = 5000
    }

    private val _eventFlows = mutableMapOf<Int, MutableSharedFlow<Any>>()

    private val responseHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GetCommentMentions.responseId -> {
                    val json = msg.data.getString(GetCommentMentions.COMMENT_MENTIONS_KEY)
                    val users = Json.decodeFromString<List<CommentMention>>(json.orEmpty(), true)

                    _eventFlows[GetCommentMentions.responseId]?.tryEmit(users.orEmpty())
                }

                GetAvatarUrls.responseId -> {
                    val json = msg.data.getString(GetAvatarUrls.RESPONSE_KEY)
                    val avatarUrls = Json.decodeFromString<Map<String, String>>(json.orEmpty(), true)

                    if (!avatarUrls.isNullOrEmpty()) {
                        avatarsCache.putAll(avatarUrls)
                        _eventFlows[GetAvatarUrls.responseId]?.tryEmit(avatarUrls)
                    }
                }
            }
        }
    }

    private val replyMessenger = Messenger(responseHandler)

    private var serviceMessenger: Messenger? = null

    private val avatarsCache: MutableMap<String, String> = mutableMapOf()

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            serviceMessenger = Messenger(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
        }
    }

    fun init(lifecycleOwner: LifecycleOwner) {
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
    fun getCommentMentions(fileId: String, filterValue: String): Flow<List<CommentMention>> {
        val flow = MutableSharedFlow<List<CommentMention>>(replay = 0, extraBufferCapacity = 1)
        val message = Message.obtain(null, GetCommentMentions.requestId)
        message.data = bundleOf(
            GetCommentMentions.FILE_ID_KEY to fileId,
            GetCommentMentions.FILTER_VALUE_KEY to filterValue
        )
        message.replyTo = replyMessenger
        serviceMessenger?.send(message)
        _eventFlows[GetCommentMentions.responseId] = flow as MutableSharedFlow<Any>
        return flow
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getAvatarUrls(userIds: List<String>): Flow<Map<String, String>> {
        val filteredIds = userIds.filterNot { it in avatarsCache.keys }
        if (filteredIds.isEmpty()) return flowOf(avatarsCache)

        waitForConnect()
        val flow = MutableSharedFlow<Map<String, String>>(replay = 0, extraBufferCapacity = 1)
        val message = Message.obtain(null, GetAvatarUrls.requestId)
        message.data = bundleOf(GetAvatarUrls.USER_IDS_KEY to filteredIds)
        message.replyTo = replyMessenger
        serviceMessenger?.send(message)
        _eventFlows[GetAvatarUrls.responseId] = flow as MutableSharedFlow<Any>
        return flow
    }

    private suspend fun waitForConnect() {
        var passedTime = 0L
        while (serviceMessenger == null && passedTime < CONNECTION_TIMEOUT) {
            val delay = 100L
            delay(delay)
            passedTime += delay
        }
    }
}