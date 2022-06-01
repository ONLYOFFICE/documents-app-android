package app.editors.manager.managers.services

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import app.editors.manager.app.appComponent
import app.editors.manager.app.loginService
import app.editors.manager.managers.utils.NotificationUtils
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.models.models.OpenFileModel
import app.editors.manager.mvp.models.models.OpenFolderModel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
                       utils.getNotification(notification.title, null, notification.body).setContentIntent(contentIntent)
                           .setAutoCancel(true)
                           .build()
                   )
               }
               message.data.containsKey("fileId") || message.data.containsKey("folderId") -> {
                   val fileId = message.data["fileId"]?.toInt()
                   val folderId = message.data["folderId"]?.toInt()

                   val model = OpenDataModel(
                       file = OpenFileModel(
                           id = fileId
                       ),
                       folder = OpenFolderModel(
                           id = folderId
                       )
                   )

//                   val uri = Uri.Builder().scheme("oodocuments").path("openfile")
//                       .appendQueryParameter("data", Json.encodeToString(model))
//                       .build()

                   val uri = Uri.parse("oodocuments://openfile?data=${Json{ encodeDefaults = true}.encodeToString(model)}&push=true#Intent;scheme=oodocuments;package=com.onlyoffice.documents;end;")

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data =uri
                    }

                   val contentIntent = PendingIntent.getActivity(
                       applicationContext,
                       0,
                       intent,
                       PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                   )

                   utils.show(
                       notification.hashCode(),
                       utils.getNotification(notification.title, null, notification.body).setContentIntent(contentIntent)
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
            applicationContext.appComponent.accountsDao.getAccounts().forEach { account ->
                val token: String? = AccountUtils.getToken(applicationContext, account.getAccountName())
                if (token != null && token.isNotEmpty()) {
                    applicationContext.loginService.setFirebaseToken(token, p0)
                        .subscribe()
                } else {
                    this.cancel()
                }
            }
        }
    }

}