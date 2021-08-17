package app.editors.manager.managers.utils;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import androidx.core.app.NotificationCompat;

import java.io.File;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.services.DownloadService;
import app.editors.manager.managers.services.UploadService;
import lib.toolkit.base.managers.utils.ActivitiesUtils;

public class NotificationUtils {

    private static final int CANCELED_GROUP_ID = 1;
    private static final String CANCELED_GROUP_KEY = "CANCELED_GROUP_KEY";

    private NotificationManager mNotificationManager;
    private Context mContext;
    private String mServiceName;

    public NotificationUtils(Context context, String serviceName) {
        mContext = context;
        mServiceName = serviceName;
        init();
    }

    private void init() {
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Need for Oreo, else doesn't work
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(mServiceName, mContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    /*
     * Notifications
     * */
    private NotificationCompat.Builder getNotificationBuilder(final String title, final String info,
                                                              final boolean isOngoing, final boolean isAlertOnce) {
        return new NotificationCompat.Builder(App.getApp(), mServiceName)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(isOngoing)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle(title)
                .setContentText(info)
                .setTicker(mContext.getString(R.string.app_name))
                .setOnlyAlertOnce(isAlertOnce)
                .setChannelId(mServiceName)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    private NotificationCompat.Builder getDownloadNotificationBuilder(final String title, final String info) {
        return new NotificationCompat.Builder(App.getApp(), mServiceName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(null)
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle(title)
                .setContentText(info)
                .setTicker(mContext.getString(R.string.app_name))
                .setChannelId(mServiceName)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    private NotificationCompat.Builder getCanceledNotificationGroupBuilder() {
        return new NotificationCompat.Builder(App.getApp(), mServiceName)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentInfo(mContext.getString(R.string.download_manager_cancel))
                .setChannelId(mServiceName)
                .setGroup(CANCELED_GROUP_KEY)
                .setGroupSummary(true);
    }

    private NotificationCompat.Builder getCanceledNotificationBuilder(final String title) {
        return new NotificationCompat.Builder(App.getApp(), mServiceName)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle(title)
                .setContentText(mContext.getString(R.string.download_manager_cancel))
                .setChannelId(mServiceName)
                .setGroup(CANCELED_GROUP_KEY);
    }

    private void showNotification(final NotificationCompat.Builder notificationBuilder, final String tag, final int id) {
        mNotificationManager.notify(tag, id, notificationBuilder.build());
    }

    public void showCanceledNotification(int id, String title) {
        mNotificationManager.notify(id, getCanceledNotificationBuilder(title).build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mNotificationManager.notify(CANCELED_GROUP_ID, getCanceledNotificationGroupBuilder().build());
        }
    }

    public void removeNotification(final String tag, final int id) {
        mNotificationManager.cancel(tag, id);
    }

    public void removeNotificationById(final int id) {
        mNotificationManager.cancel(id);
    }

    public void showForegroundNotification(final String id, final String title, final String info, final int total, final int progress, Service service) {
        final NotificationCompat.Builder builder = getNotificationBuilder(title, info, true, true);
        builder.addAction(R.drawable.drawable_ic_cancel_download_upload, mContext.getString(R.string.operation_panel_cancel_button), getUploadIntent(id))
                .setProgress(total, progress, false);
        service.startForeground(id.hashCode(), builder.build());
    }

    public void showForegroundNotification(final String tag, final int id, final String title, final String info, final int total, final int progress, Service service) {
        final NotificationCompat.Builder builder = getDownloadNotificationBuilder(title, info);
        builder.setProgress(total, progress, false)
                .addAction(R.drawable.drawable_ic_cancel_download_upload, mContext.getString(R.string.operation_panel_cancel_button), getDownloadIntent(tag));
        service.startForeground(id, builder.build());
    }

    private PendingIntent getUploadIntent(String id) {
        Intent intent = new Intent(App.getApp(), UploadService.class);
        intent.setAction(UploadService.ACTION_CANCEL);
        intent.putExtra(UploadService.TAG_ID, id);
        return PendingIntent.getService(App.getApp(), 0, intent, 0);
    }

    private PendingIntent getDownloadIntent(String id) {
        Intent intent = new Intent(App.getApp(), DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD_CANCEL);
        intent.putExtra(DownloadService.TAG_ID, id);
        return PendingIntent.getService(App.getApp(), 0, intent, 0);
    }

    public void showDownloadedNotification(final File file, final String mimeType, final String tag, final int id, final String title, final String info) {
        // Add file to default downloadFile manager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //TODO Use content resolver
        } else {
            final DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.addCompletedDownload(file.getName(), mContext.getString(R.string.download_manager_title), true,
                        mimeType, file.getPath(), file.length(), false);
            }
        }


        // On click notification - open downloadFile manager
        final PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, ActivitiesUtils.getDownloadsViewerIntent(Uri.EMPTY), 0);

        // Add action
        final NotificationCompat.Builder builder = getNotificationBuilder(title, info, false, false);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        builder.setContentIntent(contentIntent);
        showNotification(builder, tag, id);
    }

    public void showInfoNotification(final String tag, final int id, final String title, final String info) {
        final NotificationCompat.Builder builder = getNotificationBuilder(title, info, false, false);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        showNotification(builder, tag, id);
    }

    public static long getAvailableSpaceInBytes() {
        final StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return statFs.getFreeBytes();
    }
}
