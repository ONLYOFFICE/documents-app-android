package app.editors.manager.managers.services

import android.app.PendingIntent
import app.editors.manager.managers.utils.NotificationUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import lib.toolkit.base.managers.utils.ActivitiesUtils

class MessageService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let { notification ->

            // Add file to default downloadFile manager
            message.data["url"]?.let { url ->
                val contentIntent =
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        ActivitiesUtils.getBrowserIntent(url),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                    )
                val utils =
                    NotificationUtils(applicationContext, MessageService::class.java.simpleName)
                utils.show(
                    notification.hashCode(),
                    utils.getNotification(notification.title, null, notification.body).setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .build()
                )
            }

        }

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

}