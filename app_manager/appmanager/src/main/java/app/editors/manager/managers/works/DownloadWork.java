/*
 * Created by Michael Efremov on 26.10.20 13:37
 */

package app.editors.manager.managers.works;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.receivers.DownloadReceiver;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.managers.utils.NewNotificationUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.PathUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class DownloadWork extends Worker {

    private static final String TAG = DownloadWork.class.getSimpleName();

    public static String URL_KEY = "URL_KEY";
    public static String FILE_ID_KEY = "FILE_ID_KEY";
    public static String FILE_URI_KEY = "FILE_URI_KEY";

    private RetrofitTool mRetrofitTool;
    private String mToken;
    private NewNotificationUtils mNotificationUtils;
    private String mUrl;
    private DocumentFile mFile;
    private String mId;
    private Uri mTo;
    private Long mTimeMark = 0L;
    private Context mContext;

    public DownloadWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mRetrofitTool = App.getApp().getAppComponent().getRetrofit();
        mToken = App.getApp().getAppComponent().getPreference().getToken();
        mNotificationUtils = new NewNotificationUtils(getApplicationContext(), TAG);
        mContext = getApplicationContext();
    }

    @Override
    public void onStopped() {
        mNotificationUtils.removeNotification(mId.hashCode());
        super.onStopped();
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        getArgs();

        Call<ResponseBody> call;
        if (App.getApp().getAppComponent().getAccountsSql().getAccountOnline().isWebDav()) {
            AccountsSqlData sqlData = App.getApp().getAppComponent().getAccountsSql().getAccountOnline();
            call = WebDavApi.getApi(sqlData.getScheme() + sqlData.getPortal()).download(mId);
        } else {
            call = mRetrofitTool.getApiWithPreferences().downloadFile(mToken, mUrl);
        }

        try {
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                FileUtils.writeFromResponseBody(response.body(), mTo, mContext, (total, progress) -> {
                    showProgress(total, progress);
                    return isStopped();
                }, () -> {
                    mNotificationUtils.removeNotification(getId().hashCode());
                    mNotificationUtils.showCompleteNotification(getId().hashCode(), mFile.getName());
                    sendBroadcastDownloadComplete(mId, mUrl, mFile.getName(), PathUtils.getPath(mContext, mTo), StringUtils.getMimeTypeFromPath(mFile.getName()));
                }, message -> {
                    mNotificationUtils.removeNotification(getId().hashCode());
                    mFile.delete();
                    if (isStopped()) {
                        mNotificationUtils.showCanceledNotification(getId().hashCode(), mFile.getName());
                    } else {
                        mNotificationUtils.showErrorNotification(getId().hashCode(), mFile.getName());
                        sendBroadcastUnknownError(mId, mUrl, mFile.getName());
                    }
                });
            }
        } catch (IOException e) {
            mFile.delete();
            mNotificationUtils.showErrorNotification(getId().hashCode(), mFile.getName());
        }
        return Result.success();
    }

    private void showProgress(Long total, Long progress) {
        final long deltaTime = System.currentTimeMillis() - mTimeMark;
        if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
            mTimeMark = System.currentTimeMillis();
            final int percent = FileUtils.getPercentOfLoading(total, progress);
            final int id = getId().hashCode();
            final String tag = getId().toString();
            mNotificationUtils.showProgressNotification(id, tag, mFile.getName(), percent);
        }
    }

    private void getArgs() {
        final Data data = getInputData();
        mUrl = StringUtils.getEncodedString(data.getString(URL_KEY));
        mId = data.getString(FILE_ID_KEY);
        mTo = Uri.parse(data.getString(FILE_URI_KEY));
        mFile = DocumentFile.fromSingleUri(mContext, mTo);
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
    public static void sendBroadcastUnknownError(final String id, final String url, final String title) {
        Intent intent = new Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }
}
