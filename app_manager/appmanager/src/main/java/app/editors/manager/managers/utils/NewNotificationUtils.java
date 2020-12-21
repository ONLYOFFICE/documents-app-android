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
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.receivers.DownloadReceiver;
import app.editors.manager.ui.activities.main.MainActivity;
import lib.toolkit.base.managers.utils.ActivitiesUtils;

public class NewNotificationUtils {

    private static final String DOWNLOAD_GROUP = "DOWNLOAD_GROUP";
    private static final String ERROR_GROUP = "ERROR_GROUP";
    private static final String COMPLETE_GROUP = "COMPLETE_GROUP";
    private static final String CANCEL_GROUP = "CANCEL_GROUP";

    private String mServiceName;
    private Context mContext;
    private NotificationManager mNotificationManager;

    public NewNotificationUtils(Context context, String serviceName) {
        this.mServiceName = serviceName;
        this.mContext = context;
        init();
    }

    private void init() {
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Need for Oreo, else doesn't work
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(mServiceName, mContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder getDownloadNotificationBuilder(@NonNull String title) {
        return new NotificationCompat.Builder(App.getApp(), mServiceName)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentText(mContext.getString(R.string.download_manager_progress_title))
                .setTicker(mContext.getString(R.string.app_name))
                .setOnlyAlertOnce(false)
                .setChannelId(mServiceName)
                .setGroup(DOWNLOAD_GROUP)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    private NotificationCompat.Builder getNotification(String title, @Nullable String group, @Nullable String info) {
        return new NotificationCompat.Builder(App.getApp(), mServiceName)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(false)
                .setContentTitle(title)
                .setContentText(info)
                .setSmallIcon(R.drawable.ic_notify)
                .setTicker(mContext.getString(R.string.app_name))
                .setOnlyAlertOnce(false)
                .setGroup(group)
                .setChannelId(mServiceName)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    public void showProgressNotification(int id, @NonNull String tag, @NonNull String title, int progress) {
        Notification notification = getDownloadNotificationBuilder(title)
                .addAction(R.drawable.drawable_ic_cancel_download_upload, mContext.getString(R.string.operation_panel_cancel_button), getDownloadIntent(tag))
                .setProgress(100, progress, false)
                .build();
        mNotificationManager.notify(id, notification);
    }

    public void showErrorNotification(int id, @Nullable String title) {
        NotificationCompat.Builder builder = getNotification(title, ERROR_GROUP, mContext.getString(R.string.download_manager_error));
        mNotificationManager.notify(id, builder.build());
    }

    public void showCompleteNotification(java.io.File file, int id, @Nullable String title) {
        showDownloadedAction(file, id, title);
    }

    public void showCanceledNotification(int id, @Nullable String title) {
        NotificationCompat.Builder builder = getNotification(title, CANCEL_GROUP, mContext.getString(R.string.download_manager_cancel));
        mNotificationManager.notify(id, builder.build());
    }

    public void removeNotification(int id) {
        mNotificationManager.cancel(id);
    }

    private void showDownloadedAction(final File file, final int id, final String title) {
        // Add file to default downloadFile manager

        final PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, ActivitiesUtils.getDownloadsViewerIntent(), 0);

        NotificationCompat.Builder builder = getNotification(title, null, mContext.getString(R.string.download_manager_complete))
                .setGroupSummary(false)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(contentIntent);
        mNotificationManager.notify(id, builder.build());
    }

    private PendingIntent getDownloadIntent(String id) {
        final Bundle bundle = new Bundle();
        bundle.putString(DownloadReceiver.EXTRAS_KEY_ID, id);

        final Intent intent = new Intent(App.getApp(), MainActivity.class);
        intent.setAction(DownloadReceiver.DOWNLOAD_ACTION_CANCELED);
        intent.putExtras(bundle);
        return PendingIntent.getActivity(App.getApp(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
