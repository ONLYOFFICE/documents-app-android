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
import app.documents.core.di.dagger.Token
import app.documents.core.providers.RoomProvider
import app.documents.shared.di.MessengerServiceApp
import app.documents.shared.models.MessengerMessage.GetAccessToken
import app.documents.shared.models.MessengerMessage.GetSharedUsers
import app.documents.shared.models.SharedUser
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
    @Token
    lateinit var accessToken: String

    @Inject
    lateinit var accountRepository: AccountRepository

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val requestHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(message: Message) {
            when (message.what) {
                GetSharedUsers.requestId -> replySharedUsers(message)
                GetAccessToken.requestId -> replyAccessToken(message)
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

    private fun replyAccessToken(message: Message) {
        val replyMsg = Message
            .obtain(null, GetAccessToken.responseId)
            .apply {
                data = bundleOf(
                    GetAccessToken.RESPONSE_KEY to accessToken
                )
            }
        message.replyTo.send(replyMsg)
    }

    private fun replySharedUsers(message: Message) {
        val fileId = message.data.getString(GetSharedUsers.FILE_ID_KEY)
        val replyTo = message.replyTo

        coroutineScope.launch {
            try {
                val user = accountRepository.getOnlineAccount()?.let {
                    SharedUser(
                        id = it.id,
                        displayName = it.name,
                        email = "",
                        avatarUrl = "$portalUrl${it.avatarUrl}"
                    )
                }

                val sharedUsers = roomProvider
                    .getSharedUsers(requireNotNull(value = fileId) { "fileId is null" })
                    .map { user -> SharedUser.from(user, portalUrl) }

                val replyMsg = Message
                    .obtain(null, GetSharedUsers.responseId)
                    .apply {
                        data = bundleOf(
                            GetSharedUsers.RESPONSE_KEY to
                                    Json.encodeToString(sharedUsers + user, true)
                        )
                    }

                replyTo.send(replyMsg)
            } catch (e: Exception) {
                Log.e("MessengerService", e.message.toString())
            }
        }
    }
}