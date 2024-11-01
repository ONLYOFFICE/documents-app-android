package app.editors.manager.managers.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import app.editors.manager.R
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.RoomDuplicateReceiver
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.ui.activities.main.MainActivity
import lib.toolkit.base.managers.utils.ActivitiesUtils.getDownloadsViewerIntent

class NotificationUtils(private val context: Context, private val serviceName: String) {

    companion object {
        private const val DOWNLOAD_GROUP = "DOWNLOAD_GROUP"
        private const val ERROR_GROUP = "ERROR_GROUP"
        private const val COMPLETE_GROUP = "COMPLETE_GROUP"
        private const val CANCEL_GROUP = "CANCEL_GROUP"
        private const val UPLOAD_GROUP = "UPLOAD_GROUP"
        private const val DUPLICATE_ROOM_GROUP = "DUPLICATE_ROOM_GROUP"
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(NotificationManager::class.java)
    }

    init {
        // Need for Oreo, else doesn't work
        val channel = NotificationChannel(
            serviceName,
            context.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.enableVibration(false)
        notificationManager.createNotificationChannel(channel)
    }

    fun show(id: Int, notification: Notification?) {
        notificationManager.notify(id, notification)
    }

    fun getNotification(title: String?, group: String?, info: String?): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, serviceName)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT )
            .setOngoing(false)
            .setContentTitle(title)
            .setContentText(info)
            .setSmallIcon(R.drawable.ic_notify)
            .setTicker(context.getString(R.string.app_name))
            .setOnlyAlertOnce(false)
            .setGroup(group)
            .setChannelId(serviceName)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    fun showProgressNotification(id: Int, tag: String, title: String, progress: Int) {
        val notification = getDownloadNotificationBuilder(title)
            .addAction(
                R.drawable.drawable_ic_cancel_download_upload,
                context.getString(R.string.operation_panel_cancel_button),
                getDownloadIntent(tag)
            )
            .setProgress(100, progress, false)
            .setSilent(true)
            .build()
        notificationManager.notify(id, notification)
    }

    fun showArchivingProgressNotification(id: Int, tag: String, title: String, progress: Int) {
        val notification = getArchivingNotificationBuilder(title)
            .addAction(
                R.drawable.drawable_ic_cancel_download_upload,
                context.getString(R.string.operation_panel_cancel_button),
                getDownloadIntent(tag)
            )
            .setProgress(100, progress, false)
            .setSilent(true)
            .build()
        notificationManager.notify(id, notification)
    }

    fun showUploadProgressNotification(id: Int, tag: String, title: String, progress: Int) {
        val notification = getUploadNotificationBuilder(title)
            .addAction(
                R.drawable.drawable_ic_cancel_download_upload,
                context.getString(R.string.operation_panel_cancel_button),
                getUploadIntent(tag)
            )
            .setProgress(100, progress, false)
            .setSilent(true)
            .build()
        notificationManager.notify(id, notification)
    }

    fun showRoomDuplicateProgressNotification(id: Int, workerId: String, title: String, progress: Int) {
        val notification = getRoomDuplicateNotificationBuilder(title)
            .addAction(
                R.drawable.drawable_ic_cancel_download_upload,
                context.getString(R.string.room_duplicate_progress_hide_button),
                getRoomDuplicateIntent(workerId)
            )
            .setProgress(100, progress, false)
            .setSilent(true)
            .build()
        notificationManager.notify(id, notification)
    }

    fun showErrorNotification(id: Int, title: String?) {
        val builder = getNotification(title, ERROR_GROUP, context.getString(R.string.download_manager_error))
        notificationManager.notify(id, builder.build())
    }

    fun showUploadErrorNotification(
        id: Int,
        title: String?,
        message: Int = R.string.upload_manager_error
    ) {
        val builder = getNotification(title, ERROR_GROUP, context.getString(message))
        notificationManager.notify(id, builder.build())
    }

    fun showRoomDuplicateErrorNotification(id: Int, title: String?) {
        val builder = getNotification(title, ERROR_GROUP, context.getString(R.string.room_duplicate_error))
        notificationManager.notify(id, builder.build())
    }

    fun showCompleteNotification(id: Int, title: String?, uri: Uri) {
        showDownloadedAction(id, title, uri)
    }

    fun showUploadCompleteNotification(id: Int, title: String?) {
        showUploadedAction(id, title)
    }

    fun showRoomDuplicateCompleteNotification(id: Int, title: String?) {
        showRoomDuplicateAction(id, title)
    }

    fun showCanceledNotification(id: Int, title: String?) {
        val builder = getNotification(title, CANCEL_GROUP, context.getString(R.string.download_manager_cancel))
        notificationManager.notify(id, builder.build())
    }

    fun showCanceledUploadNotification(id: Int, title: String?) {
        val builder = getNotification(title, CANCEL_GROUP, context.getString(R.string.upload_manager_cancel))
        notificationManager.notify(id, builder.build())
    }

    fun removeNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun getDownloadNotificationBuilder(title: String): NotificationCompat.Builder =
        getNotificationBuilder(title, R.string.download_manager_progress_title, DOWNLOAD_GROUP)

    private fun getArchivingNotificationBuilder(title: String): NotificationCompat.Builder =
        getNotificationBuilder(title, R.string.download_manager_archiving_progress, DOWNLOAD_GROUP)

    private fun getUploadNotificationBuilder(title: String): NotificationCompat.Builder =
        getNotificationBuilder(title, R.string.upload_manager_progress_title, UPLOAD_GROUP)

    private fun getRoomDuplicateNotificationBuilder(title: String): NotificationCompat.Builder =
        getNotificationBuilder(title, R.string.room_duplicate_progress_title, DUPLICATE_ROOM_GROUP)

    private fun getNotificationBuilder(
        title: String,
        @StringRes contentText: Int,
        group: String
    ): NotificationCompat.Builder = NotificationCompat.Builder(context, serviceName)
        .setPriority(NotificationManager.IMPORTANCE_DEFAULT )
        .setOngoing(true)
        .setContentTitle(title)
        .setSmallIcon(R.drawable.ic_notify)
        .setContentText(context.getString(contentText))
        .setTicker(context.getString(R.string.app_name))
        .setOnlyAlertOnce(true)
        .setChannelId(serviceName)
        .setGroup(group)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    private fun showDownloadedAction(id: Int, title: String?, uri: Uri) {
        // Add file to default downloadFile manager
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            getDownloadsViewerIntent(uri),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = getNotification(title, null, context.getString(R.string.download_manager_complete))
            .setGroupSummary(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
        notificationManager.notify(id, builder.build())
    }

    private fun showUploadedAction(id: Int, title: String?) {
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = getNotification(title, null, context.getString(R.string.upload_manager_complete))
            .setGroupSummary(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(contentIntent)
        notificationManager.notify(id, builder.build())
    }

    private fun showRoomDuplicateAction(id: Int, title: String?) {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = getNotification(title, null, context.getString(R.string.room_duplicate_complete))
            .setGroupSummary(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(contentIntent)
        notificationManager.notify(id, builder.build())
    }

    private fun getDownloadIntent(id: String): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            action = DownloadReceiver.DOWNLOAD_ACTION_CANCELED
            putExtras(bundleOf(DownloadReceiver.EXTRAS_KEY_ID to id))
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun getUploadIntent(id: String): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            action = UploadReceiver.UPLOAD_ACTION_CANCELED
            putExtras(bundleOf(UploadReceiver.EXTRAS_KEY_ID to id))
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun getRoomDuplicateIntent(workerId: String): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            action = RoomDuplicateReceiver.ACTION_HIDE
            putExtra(RoomDuplicateReceiver.KEY_NOTIFICATION_HIDE, workerId)
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}