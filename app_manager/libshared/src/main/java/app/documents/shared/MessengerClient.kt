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
import app.documents.core.network.common.contracts.ApiContract
import app.documents.shared.models.MessengerMessage.GetAccessToken
import app.documents.shared.models.MessengerMessage.GetSharedUsers
import app.documents.shared.models.MessengerMessage.GetUsersAvatars
import app.documents.shared.models.MessengerMessage.SendMentionNotifications
import app.documents.shared.models.SharedUser
import app.documents.shared.utils.decodeFromString
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json

class MessengerClient(private val context: Context) {

    companion object {

        private const val CONNECTION_TIMEOUT = 5000
    }

    private val callbacks = mutableMapOf<Int, (Message) -> Unit>()

    private val responseHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(message: Message) {
            callbacks[message.what]?.invoke(message)
        }
    }

    private val replyMessenger = Messenger(responseHandler)

    private var serviceMessenger: Messenger? = null
    private var sharedUsersCache: List<SharedUser>? = null
    private var accessToken: String? = null

    private var avatarsCache: MutableMap<String, GlideUrl> = mutableMapOf()

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
    fun getSharedUsers(fileId: String, filterValue: String = ""): Flow<List<SharedUser>> =
        callbackFlow {
            sharedUsersCache?.let { cache ->
                trySend(cache.filter { it.filterByNameOrEmail(filterValue) })
                close()
            }

            waitForConnect()
            checkAccessToken { accessToken ->
                val message = Message.obtain(null, GetSharedUsers.requestId)
                message.data = bundleOf(GetSharedUsers.FILE_ID_KEY to fileId)
                message.replyTo = replyMessenger

                callbacks[GetSharedUsers.responseId] = { message ->
                    val json = message.data.getString(GetSharedUsers.RESPONSE_KEY)
                    val users = Json.decodeFromString<List<SharedUser>>(json.orEmpty(), true)

                    if (!users.isNullOrEmpty()) {
                        val userWithAvatars = users
                            .filter { it.filterByNameOrEmail(filterValue) }
                            .map {
                                val glideUrl = getGlideUrl(it.avatarUrl, accessToken)
                                avatarsCache.putIfAbsent(it.id, glideUrl)
                                it.copy(avatarGlideUrl = glideUrl)
                            }

                        sharedUsersCache = userWithAvatars
                        trySend(userWithAvatars)
                        close()
                    }
                }

                serviceMessenger?.send(message)
            }

            awaitClose {
                callbacks.remove(GetSharedUsers.responseId)
            }
        }

    fun sendMentionNotifications(fileId: String, emails: Set<String>, comment: String) {
        val message = Message.obtain(null, SendMentionNotifications.requestId)

        message.data = bundleOf(
            SendMentionNotifications.FILE_ID_KEY to fileId,
            SendMentionNotifications.EMAILS_KEY to ArrayList(emails),
            SendMentionNotifications.COMMENT_KEY to comment,
        )

        serviceMessenger?.send(message)
    }

    fun getUsersAvatars(userIds: List<String>): Flow<Map<String, GlideUrl>> = callbackFlow {
        val notCachedAvatars = userIds
            .mapNotNull { id -> id.takeUnless { avatarsCache.contains(it) } }

        if (notCachedAvatars.isEmpty()) {
            trySend(avatarsCache)
            close()
        }

        waitForConnect()
        checkAccessToken { accessToken ->
            val message = Message.obtain(null, GetUsersAvatars.requestId)

            message.data = bundleOf(GetUsersAvatars.USERS_ID_KEY to ArrayList(notCachedAvatars))
            message.replyTo = replyMessenger

            callbacks[GetUsersAvatars.responseId] = { message ->
                val json = message.data.getString(GetUsersAvatars.RESPONSE_KEY).orEmpty()
                val response = Json.decodeFromString<Map<String, String>>(json, true)

                if (!response.isNullOrEmpty()) {
                    val mappedToGlideUrl = response
                        .mapValues { (_, avatarUrl) -> getGlideUrl(avatarUrl, accessToken) }
                    avatarsCache.putAll(mappedToGlideUrl)
                    trySend(avatarsCache)
                    close()
                }
            }

            serviceMessenger?.send(message)
        }

        awaitClose {
            callbacks.remove(GetSharedUsers.responseId)
        }
    }

    private fun getGlideUrl(avatarUrl: String?, accessToken: String): GlideUrl {
        return GlideUrl(
            avatarUrl,
            LazyHeaders.Builder()
                .addHeader(ApiContract.HEADER_AUTHORIZATION, accessToken)
                .build()
        )
    }

    private fun checkAccessToken(block: (String) -> Unit) {
        accessToken?.let { token ->
            block(token)
            return
        }

        val message = Message.obtain(null, GetAccessToken.requestId)
        message.replyTo = replyMessenger

        callbacks[GetAccessToken.responseId] = { message ->
            val token = message.data.getString(GetAccessToken.RESPONSE_KEY).orEmpty()
            accessToken = token
            block(token)
            callbacks.remove(GetAccessToken.responseId)
        }

        serviceMessenger?.send(message)
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