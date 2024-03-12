package app.editors.manager.managers.services

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.app.appComponent
import app.editors.manager.app.coreComponent
import app.editors.manager.managers.utils.NotificationUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ActivitiesUtils

class MessageService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val utils =
            NotificationUtils(applicationContext, MessageService::class.java.simpleName)
        message.notification?.let { notification ->

            // Add file to default downloadFile manager
            when {
                message.data.containsKey("url") -> {
                    val contentIntent =
                        PendingIntent.getActivity(
                            applicationContext,
                            0,
                            ActivitiesUtils.getBrowserIntent(checkNotNull(message.data["url"])),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    utils.show(
                        notification.hashCode(),
                        utils.getNotification(notification.title, null, notification.body)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true)
                            .build()
                    )
                }
                message.data.containsKey("data") -> {
                    val model = message.data["data"]?.replace("#", "")

                    val uri =
                        Uri.parse("${BuildConfig.PUSH_SCHEME}://openfile?data=${model}&push=true#Intent;scheme=${BuildConfig.PUSH_SCHEME};package=com.onlyoffice.documents;end;")

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        data = uri
                    }

                    val contentIntent = PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                    )

                    utils.show(
                        notification.hashCode(),
                        utils.getNotification(notification.title, null, notification.body)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true)
                            .build()
                    )
                }
                else -> {
                    utils.show(
                        notification.hashCode(),
                        utils.getNotification(notification.title, null, notification.body)
                            .setAutoCancel(true)
                            .build()
                    )
                }
            }

        }

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        applicationContext.appComponent.preference.deviceMessageToken = p0
        CoroutineScope(Dispatchers.Default).launch {
            App.getApp().refreshLoginComponent(null)
            applicationContext.coreComponent.cloudDataSource.getAccounts().forEach { account ->
                val token: String? = AccountUtils.getToken(applicationContext, account.accountName)
                if (!token.isNullOrEmpty()) {
                    App.getApp().loginComponent.loginRepository.registerDevice(account.portal.urlWithScheme, token, p0)
                } else {
                    this.cancel()
                }
            }
        }
    }

}