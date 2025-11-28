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
import app.documents.core.account.AccountRepository
import app.documents.core.providers.RoomProvider
import app.documents.shared.di.MessengerServiceApp
import app.documents.shared.models.CommentMention
import app.documents.shared.models.MessengerMessage.GetAvatarUrls
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

    @Inject
    lateinit var accountRepository: AccountRepository

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val requestHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(message: Message) {
            when (message.what) {
                GetCommentMentions.requestId -> replyCommentMentions(message)
                GetAvatarUrls.requestId -> replyAvatarUrls(message)
            }
        }
    }

    private val app: MessengerServiceApp? by lazy { application as? MessengerServiceApp }
    private val messenger = Messenger(requestHandler)

    private var portalUrl: String? = null

    override fun onBind(intent: Intent): IBinder = messenger.binder

    override fun onCreate() {
        super.onCreate()
        val component = app?.createMessengerServiceComponent()
        component?.inject(this)
        coroutineScope.launch {
            portalUrl = accountRepository.getOnlineAccount()?.portal?.urlWithScheme
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        app?.destroyMessengerServiceComponent()
    }

    private fun replyAvatarUrls(message: Message) {
        val users = message.data.getStringArrayList(GetAvatarUrls.USER_IDS_KEY)?.toList() ?: return
        val replyTo = message.replyTo

        coroutineScope.launch {
            try {
                requireNotNull(portalUrl)
                val avatars = users.map { "$portalUrl${roomProvider.getUserProfile(it).avatarUrl}" }
                val userAvatarsMap = users.zip(avatars).toMap()
                val replyMsg = Message
                    .obtain(null, GetAvatarUrls.responseId)
                    .apply {
                        data = bundleOf(
                            GetAvatarUrls.RESPONSE_KEY to Json.encodeToString(userAvatarsMap, true)
                        )
                    }

                replyTo.send(replyMsg)
            } catch (e: Exception) {
                Log.e("MessengerService", e.message.toString())
            }
        }
    }

    private fun replyCommentMentions(message: Message) {
        val fileId = message.data.getString(GetCommentMentions.FILE_ID_KEY)
        val filterValue = message.data.getString(GetCommentMentions.FILTER_VALUE_KEY)
        val replyTo = message.replyTo
        coroutineScope.launch {
            try {
                val users = roomProvider
                    .getUsersByItemId(
                        requireNotNull(value = fileId) { "fileId is null" },
                        isFolder = false,
                        filterValue = filterValue.orEmpty()
                    )
                    .map(CommentMention::from)

                val replyMsg = Message
                    .obtain(null, GetCommentMentions.responseId)
                    .apply {
                        data = bundleOf(
                            GetCommentMentions.COMMENT_MENTIONS_KEY to
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