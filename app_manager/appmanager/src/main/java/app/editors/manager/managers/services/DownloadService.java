package app.editors.manager.managers.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.managers.receivers.DownloadReceiver;
import app.editors.manager.managers.utils.NotificationUtils;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@Deprecated
public class DownloadService extends Service {

    public static final String TAG = DownloadService.class.getSimpleName();
    private static final String PATH_DOWNLOAD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OnlyOffice";

    private final String ERRORS_UNKNOWN = "ERRORS_UNKNOWN";
    private final String ERRORS_FREE_SPACE = "ERRORS_FREE_SPACE";

    public static final String TAG_URI = "TAG_URI";
    public static final String TAG_ID = "TAG_ID";
    public static final String TAG_TITLE = "TAG_TITLE";
    public static final String TAG_EXT = "TAG_EXT";
    public static final String TAG_NOTIFICATION = "TAG_NOTIFICATION";
    public static final String TAG_TEMP = "TAG_TEMP";

    public static final String ACTION_DOWNLOAD = "ACTION_DOWNLOAD";
    public static final String ACTION_DOWNLOAD_CANCEL = "ACTION_DOWNLOAD_CANCEL";

    private static final String TEMP_FILE_NAME = "onlyoffice_temp";

    private boolean isDownload;

    private static Map<String, AsyncDownload> sDownloading;


    private NotificationUtils mNotificationUtils;

    private LinkedList<AsyncDownload> mAsyncDownloads = new LinkedList<>();

    static {
        sDownloading = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mAsyncDownloads = null;
        sDownloading.clear();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_DOWNLOAD:
                download(intent);
                break;
            case ACTION_DOWNLOAD_CANCEL:
                cancelDownload(intent);
                break;
        }
        return START_REDELIVER_INTENT;
    }

    private void init() {
        mNotificationUtils = new NotificationUtils(App.getApp(), TAG);
    }

    private void download(final Intent intent) {
        final String uri = intent.getStringExtra(TAG_URI);
        final String id = intent.getStringExtra(TAG_ID);
        final String title = intent.getStringExtra(TAG_TITLE);
        final String ext = intent.getStringExtra(TAG_EXT);
        final boolean isNotification = intent.getBooleanExtra(TAG_NOTIFICATION, true);
        final boolean isTemp = intent.getBooleanExtra(TAG_TEMP, false);

        if (!sDownloading.containsKey(id)) {
            try {
                final AsyncDownload asyncDownload = new AsyncDownload(isNotification, isTemp, id, uri, title, ext, this);
                mAsyncDownloads.add(asyncDownload);
                if (!isDownload) {
                    isDownload = true;
                    mAsyncDownloads.getFirst().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    sDownloading.put(id, asyncDownload);
                }
            } catch (UrlSyntaxMistake | IOException e) {
                sendBroadcastInitError(id, uri, title);
            }
        } else {
            sendBroadcastRepeat(id, title);
        }
    }

    private void cancelDownload(final Intent intent) {
        final String id = intent.getStringExtra(TAG_ID);
        final AsyncDownload asyncDownload = mAsyncDownloads.peekFirst();
        mNotificationUtils.removeNotificationById(id.hashCode());
        if (asyncDownload == null) {
            sendBroadcastDownloadCanceled(id, DownloadReceiver.EXTRAS_VALUE_CANCELED_NOT_FOUND);
        } else {
            mNotificationUtils.showCanceledNotification(asyncDownload.mTitle.hashCode(), asyncDownload.mTitle);
            asyncDownload.cancel(true);
            sendBroadcastDownloadCanceled(id, DownloadReceiver.EXTRAS_VALUE_CANCELED);
        }
    }

    /*
     * Asynchronous file downloadFile class
     * */
    private class AsyncDownload extends AsyncTask<Void, Long, String> {

        private static final int BLOCK_SIZE = 1024 * 4;
        private static final int BUFFER_SIZE = 1024 * 8;

        public Call<ResponseBody> mBodyCall;
        public final String mId;
        public final String mUri;
        public final String mTitle;
        public final boolean mIsNotification;
        public final boolean mIsTemp;

        private File mOutputFile;
        private Service service;
        private String mMimeType;
        private long mTimeMark;

        public AsyncDownload(final boolean isNotification, final boolean isTemp, final String id, final String uri, final String title, final String extension,
                             Service service) throws UrlSyntaxMistake, IOException {
            mIsNotification = isNotification;
            mIsTemp = isTemp;
            mUri = StringUtils.getEncodedString(uri.trim());
            mId = id;
            mTitle = title;
            mMimeType = StringUtils.getMimeTypeFromExtension(extension);
//            if (App.getApp().getAppComponent()   .getAccountsSql().getAccountOnline().isWebDav()) {
//                AccountsSqlData sqlData = App.getApp().getAppComponent().getAccountsSql().getAccountOnline();
//                mBodyCall = WebDavApi.getApi(sqlData.getScheme() + sqlData.getPortal()).download(id);
//            } else {
//                mBodyCall = mRetrofitTool.getApiWithPreferences().downloadFile(mPreferenceTool.getToken(), mUri, Api.COOKIE_HEADER + mPreferenceTool.getToken());
//            }
            this.service = service;

            if (mIsTemp) {
                mOutputFile = File.createTempFile(TEMP_FILE_NAME, extension);
            } else {
                mOutputFile = new File(PATH_DOWNLOAD, mTitle);
                if (mOutputFile.exists()) {
                    mOutputFile = FileUtils.getNewFileName(mOutputFile);
                }
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                final Response<ResponseBody> bodyResponse = mBodyCall.execute();
                final ResponseBody responseBody = bodyResponse.body();
                final byte[] buffer = new byte[BLOCK_SIZE];
                if (responseBody == null) {
                    return ERRORS_UNKNOWN;
                }
                final long fileSize = responseBody.contentLength();
                final long freeSize = NotificationUtils.getAvailableSpaceInBytes();

                // Check available free space
                if (fileSize > freeSize * 1.2) {
                    return ERRORS_FREE_SPACE;
                }

                // Init streams
                inputStream = new BufferedInputStream(responseBody.byteStream(), BUFFER_SIZE);
                outputStream = new FileOutputStream(mOutputFile);

                // Downloading with progress
                int countBytes;
                long totalBytes = 0;
                while ((countBytes = inputStream.read(buffer)) != -1) {
                    totalBytes += countBytes;
                    outputStream.write(buffer, 0, countBytes);

                    // Update progress
                    long deltaTime = System.currentTimeMillis() - mTimeMark;
                    if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
                        mTimeMark = System.currentTimeMillis();
                        publishProgress(fileSize, totalBytes);
                    }
                }

                outputStream.flush();
            } catch (Exception e) {
                Log.e(TAG, AsyncDownload.class.getSimpleName(), e);
                mAsyncDownloads.pollFirst().cancel(true);
                return ERRORS_UNKNOWN;
            } finally {
                // Close all streams
                try {
                    inputStream.close();
                } catch (Exception e) {
                    // No need handle
                }

                try {
                    outputStream.close();
                } catch (Exception e) {
                    // No need handle
                }
            }

            return null;
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            // Remove cancel task
            sDownloading.remove(mId);
            isDownload = false;
            // Cancel request
            if (mBodyCall != null) {
                mBodyCall.cancel();
            }
            if (!mAsyncDownloads.isEmpty()) {
                mAsyncDownloads.peekFirst().execute();
            } else {
                stopSelf();
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            final long total = values[0];
            final long downloaded = values[1];
            final int percent = FileUtils.getPercentOfLoading(total, downloaded);
            sendBroadcastProgress(mId, FileUtils.LOAD_MAX_PROGRESS, percent);

            if (mIsNotification) {
                mNotificationUtils.showForegroundNotification(mId, mId.hashCode(), mTitle, getString(R.string.download_manager_progress_title),
                        FileUtils.LOAD_MAX_PROGRESS, percent, service);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Remove finished task
            sDownloading.remove(mId);
            mNotificationUtils.removeNotificationById(mId.hashCode());

            // Handle result
            if (result == null) {
                sendBroadcastDownloadComplete(mId, mUri, mTitle, mOutputFile.getPath(), mMimeType);

                if (mIsNotification) {
                    mNotificationUtils.showDownloadedNotification(mOutputFile, mMimeType, mId, mId.hashCode(),
                            getString(R.string.download_manager_complete), mTitle);
                }
                if (mAsyncDownloads != null && !mAsyncDownloads.isEmpty()) {
                    mAsyncDownloads.removeFirst();
                }
                isDownload = false;
                if (!mAsyncDownloads.isEmpty()) {
                    mAsyncDownloads.getFirst().execute();
                } else {
                    stopSelf();
                }
            } else {
                // TODO add new handle errors here
                switch (result) {
                    case ERRORS_UNKNOWN:
                        sendBroadcastUnknownError(mId, mUri, mTitle);
                        mAsyncDownloads.pollFirst().cancel(true);
                        stopSelf();
                        if (mIsNotification) {
                            mNotificationUtils.showInfoNotification(mId, mId.hashCode(), getString(R.string.download_manager_error), mTitle);
                        }
                        break;
                    case ERRORS_FREE_SPACE:
                        sendBroadcastErrorFreeSpace();
                        break;
                }
            }
        }
    }

    /*
     * Broadcasts callbacks
     * */
    public static void sendBroadcastUnknownError(final String id, final String url, final String title) {
        Intent intent = new Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    public static void sendBroadcastErrorFreeSpace() {
        Intent intent = new Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    public static void sendBroadcastInitError(final String id, final String url, final String title) {
        Intent intent = new Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR_URL_INIT);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    public static void sendBroadcastDownloadComplete(final String id, final String url, final String title,
                                                     final String path, final String mime) {
        Intent intent = new Intent(DownloadReceiver.DOWNLOAD_ACTION_COMPLETE);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_PATH, path);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_MIME_TYPE, mime);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    public static void sendBroadcastDownloadCanceled(final String id, final int value) {
        Intent intent = new Intent(DownloadReceiver.DOWNLOAD_ACTION_CANCELED);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_CANCELED, value);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    public static void sendBroadcastRepeat(final String id, final String title) {
        Intent intent = new Intent(DownloadReceiver.DOWNLOAD_ACTION_REPEAT);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    public static void sendBroadcastProgress(final String id, final int total, final int progress) {
        Intent intent = new Intent(DownloadReceiver.DOWNLOAD_ACTION_PROGRESS);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_PROGRESS, progress);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TOTAL, total);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    /*
     * Actions for service
     * */
    public static void startDownload(final boolean isNotification, final boolean isTemp,
                                     final String id, final String url, final String title, final String extension) {
        final Intent intent = new Intent(App.getApp(), DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(TAG_ID, id);
        intent.putExtra(TAG_URI, url);
        intent.putExtra(TAG_TITLE, title);
        intent.putExtra(TAG_EXT, extension);
        intent.putExtra(TAG_NOTIFICATION, isNotification);
        intent.putExtra(TAG_TEMP, isTemp);
        App.getApp().startService(intent);
    }

    public static void cancelDownload(final String id) {
        final Intent intent = new Intent(App.getApp(), DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD_CANCEL);
        intent.putExtra(TAG_ID, id);
        App.getApp().startService(intent);
    }

}