/*
 * Created by Michael Efremov on 26.10.20 13:23
 */

package app.editors.manager.managers.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.receivers.DownloadReceiver;
import app.editors.manager.managers.receivers.UploadReceiver;
import app.editors.manager.ui.activities.main.MainActivity;
import lib.toolkit.base.managers.utils.ActivitiesUtils;

public class NotificationUtils {

    private static final String DOWNLOAD_GROUP = "DOWNLOAD_GROUP";
    private static final String ERROR_GROUP = "ERROR_GROUP";
    private static final String COMPLETE_GROUP = "COMPLETE_GROUP";
    private static final String CANCEL_GROUP = "CANCEL_GROUP";
    private static final String UPLOAD_GROUP = "UPLOAD_GROUP";

    private String serviceName;
    private Context context;
    private NotificationManager notificationManager;

    public NotificationUtils(Context context, String serviceName) {
        this.serviceName = serviceName;
        this.context = context;
        init();
    }

    private void init() {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Need for Oreo, else doesn't work
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(serviceName, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void show(int id, Notification notification) {
        notificationManager.notify(id, notification);
    }

    private NotificationCompat.Builder getDownloadNotificationBuilder(@NonNull String title) {
        return new NotificationCompat.Builder(App.getApp(), serviceName)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentText(context.getString(R.string.download_manager_progress_title))
                .setTicker(context.getString(R.string.app_name))
                .setOnlyAlertOnce(true)
                .setChannelId(serviceName)
                .setGroup(DOWNLOAD_GROUP)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    private NotificationCompat.Builder getArchivingNotificationBuilder(@NonNull String title) {
        return new NotificationCompat.Builder(App.getApp(), serviceName)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentText(context.getString(R.string.download_manager_archiving_progress))
                .setTicker(context.getString(R.string.app_name))
                .setOnlyAlertOnce(true)
                .setChannelId(serviceName)
                .setGroup(DOWNLOAD_GROUP)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }
    private NotificationCompat.Builder getUploadNotificationBuilder(@NonNull String title) {
        return new NotificationCompat.Builder(App.getApp(), serviceName)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentText(context.getString(R.string.upload_manager_progress_title))
                .setTicker(context.getString(R.string.app_name))
                .setOnlyAlertOnce(true)
                .setChannelId(serviceName)
                .setGroup(UPLOAD_GROUP)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    public NotificationCompat.Builder getNotification(String title, @Nullable String group, @Nullable String info) {
        return new NotificationCompat.Builder(App.getApp(), serviceName)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(false)
                .setContentTitle(title)
                .setContentText(info)
                .setSmallIcon(R.drawable.ic_notify)
                .setTicker(context.getString(R.string.app_name))
                .setOnlyAlertOnce(false)
                .setGroup(group)
                .setChannelId(serviceName)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    public void showProgressNotification(int id, @NonNull String tag, @NonNull String title, int progress) {
        Notification notification = getDownloadNotificationBuilder(title)
                .addAction(R.drawable.drawable_ic_cancel_download_upload, context.getString(R.string.operation_panel_cancel_button), getDownloadIntent(tag))
                .setProgress(100, progress, false)
                .build();
        notificationManager.notify(id, notification);
    }

    public void showArchivingProgressNotification(int id, @NonNull String tag, @NonNull String title, int progress) {
        Notification notification = getArchivingNotificationBuilder(title)
                .addAction(R.drawable.drawable_ic_cancel_download_upload, context.getString(R.string.operation_panel_cancel_button), getDownloadIntent(tag))
                .setProgress(100, progress, false)
                .build();
        notificationManager.notify(id, notification);
    }

    public void showUploadProgressNotification(int id, @NonNull String tag, @NonNull String title, int progress) {
        Notification notification = getUploadNotificationBuilder(title)
                .addAction(R.drawable.drawable_ic_cancel_download_upload, context.getString(R.string.operation_panel_cancel_button), getUploadIntent(tag))
                .setProgress(100, progress, false)
                .build();
        notificationManager.notify(id, notification);
    }

    public void showErrorNotification(int id, @Nullable String title) {
        NotificationCompat.Builder builder = getNotification(title, ERROR_GROUP, context.getString(R.string.download_manager_error));
        notificationManager.notify(id, builder.build());
    }

    public void showUploadErrorNotification(int id, @Nullable String title) {
        NotificationCompat.Builder builder = getNotification(title, ERROR_GROUP, context.getString(R.string.upload_manager_error));
        notificationManager.notify(id, builder.build());
    }

    public void showCompleteNotification(int id, @Nullable String title, Uri uri) {
        showDownloadedAction(id, title, uri);
    }

    public void showUploadCompleteNotification(int id, @Nullable String title) {
        showUploadedAction(id, title);
    }

    public void showCanceledNotification(int id, @Nullable String title) {
        NotificationCompat.Builder builder = getNotification(title, CANCEL_GROUP, context.getString(R.string.download_manager_cancel));
        notificationManager.notify(id, builder.build());
    }
    public void showCanceledUploadNotification(int id, @Nullable String title) {
        NotificationCompat.Builder builder = getNotification(title, CANCEL_GROUP, context.getString(R.string.upload_manager_cancel));
        notificationManager.notify(id, builder.build());
    }

    public void removeNotification(int id) {
        notificationManager.cancel(id);
    }

    private void showDownloadedAction(final int id, final String title, final Uri uri) {
        // Add file to default downloadFile manager

        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, ActivitiesUtils.getDownloadsViewerIntent(uri), 0);

        NotificationCompat.Builder builder = getNotification(title, null, context.getString(R.string.download_manager_complete))
                .setGroupSummary(false)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        notificationManager.notify(id, builder.build());
    }

    private void showUploadedAction(final int id, final String title) {

        Intent mainActivityIntent = new Intent(context, MainActivity.class);

        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);

        NotificationCompat.Builder builder = getNotification(title, null, context.getString(R.string.upload_manager_complete))
                .setGroupSummary(false)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(contentIntent);
        notificationManager.notify(id, builder.build());
    }

    private PendingIntent getDownloadIntent(String id) {
        final Bundle bundle = new Bundle();
        bundle.putString(DownloadReceiver.EXTRAS_KEY_ID, id);

        final Intent intent = new Intent(App.getApp(), MainActivity.class);
        intent.setAction(DownloadReceiver.DOWNLOAD_ACTION_CANCELED);
        intent.putExtras(bundle);
        return PendingIntent.getActivity(App.getApp(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getUploadIntent(String id) {
        final Bundle bundle = new Bundle();
        bundle.putString(UploadReceiver.EXTRAS_KEY_ID, id);

        final Intent intent = new Intent(App.getApp(), MainActivity.class);
        intent.setAction(UploadReceiver.UPLOAD_ACTION_CANCELED);
        intent.putExtras(bundle);
        return PendingIntent.getActivity(App.getApp(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
