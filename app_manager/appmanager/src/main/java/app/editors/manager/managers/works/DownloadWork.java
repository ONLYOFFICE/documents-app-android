/*
 * Created by Michael Efremov on 26.10.20 13:37
 */

package app.editors.manager.managers.works;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.receivers.DownloadReceiver;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.managers.utils.NewNotificationUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.base.Download;
import app.editors.manager.mvp.models.explorer.Operation;
import app.editors.manager.mvp.models.request.RequestDownload;
import app.editors.manager.mvp.models.response.ResponseDownload;
import io.reactivex.Single;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.PathUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

public class DownloadWork extends Worker {

    private static final String TAG = DownloadWork.class.getSimpleName();

    public static String URL_KEY = "URL_KEY";
    public static String FILE_ID_KEY = "FILE_ID_KEY";
    public static String FILE_URI_KEY = "FILE_URI_KEY";
    public static String REQUEST_DOWNLOAD = "REQUEST_DOWNLOAD";

    private static final String KEY_ERROR_INFO = "error";
    private static final String KEY_ERROR_INFO_MESSAGE = "message";

    private RetrofitTool mRetrofitTool;
    private String mToken;
    private NewNotificationUtils mNotificationUtils;
    private String mUrl;
    private DocumentFile mFile;
    private String mId;
    private Uri mTo;
    private Long mTimeMark = 0L;
    private Context mContext;
    private RequestDownload mRequestDownload;

    public DownloadWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mRetrofitTool = App.getApp().getAppComponent().getRetrofit();
        mToken = App.getApp().getAppComponent().getPreference().getToken();
        mNotificationUtils = new NewNotificationUtils(getApplicationContext(), TAG);
        mContext = getApplicationContext();
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        getArgs();

        if(mRequestDownload != null) {
            downloadFiles();
        }

        Call<ResponseBody> call;
        if (App.getApp().getAppComponent().getAccountsSql().getAccountOnline().isWebDav()) {
            AccountsSqlData sqlData = App.getApp().getAppComponent().getAccountsSql().getAccountOnline();
            call = WebDavApi.getApi(sqlData.getScheme() + sqlData.getPortal()).download(mId);
        } else {
            if(mUrl != null) {
                call = mRetrofitTool.getApiWithPreferences().downloadFile(mToken, mUrl);
            } else {
                mFile.delete();
                return Result.failure();
            }
        }

        try {
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                FileUtils.writeFromResponseBody(response.body(), mTo, mContext, (total, progress) -> {
                    showProgress(total, progress, false);
                    return isStopped();
                }, () -> {
                    mNotificationUtils.removeNotification(getId().hashCode());
                    mNotificationUtils.showCompleteNotification(getId().hashCode(), mFile.getName());
                    sendBroadcastDownloadComplete(mId, mUrl, mFile.getName(), PathUtils.getPath(mContext, mTo), StringUtils.getMimeTypeFromPath(mFile.getName()));
                }, message -> {
                    mNotificationUtils.removeNotification(getId().hashCode());
                    if (isStopped()) {
                        mNotificationUtils.showCanceledNotification(getId().hashCode(), mFile.getName());
                        mFile.delete();
                    } else {
                        mNotificationUtils.showErrorNotification(getId().hashCode(), mFile.getName());
                        sendBroadcastUnknownError(mId, mUrl, mFile.getName());
                        mFile.delete();
                    }
                });
            }
        } catch (IOException e) {
            mNotificationUtils.showErrorNotification(getId().hashCode(), mFile.getName());
        }

        return Result.success();
    }

    private void showProgress(Long total, Long progress, boolean isArchiving) {
        final long deltaTime = System.currentTimeMillis() - mTimeMark;
        if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
            mTimeMark = System.currentTimeMillis();
            final int percent = FileUtils.getPercentOfLoading(total, progress);
            final int id = getId().hashCode();
            final String tag = getId().toString();
            if(!isArchiving) {
                mNotificationUtils.showProgressNotification(id, tag, mFile.getName(), percent);
            } else {
                mNotificationUtils.showArchivingProgressNotification(id, tag, mFile.getName(), percent);
            }
        }
    }

    private void downloadFiles() {
        Single<ResponseDownload> responseDownloadCall = mRetrofitTool.getApiWithPreferences().downloadFiles(mToken, mRequestDownload);
        try {
            ResponseDownload response = responseDownloadCall.blockingGet();
            List<Download> downloads = response.getResponse();
            for (Download download : downloads) {
                do {
                    if (!isStopped()) {
                        List<Operation> operations = mRetrofitTool.getApiWithPreferences().status(mToken).blockingGet().getResponse();
                        if (!operations.isEmpty()) {
                            if (operations.get(0).getError() == null) {
                                showProgress((long) FileUtils.LOAD_MAX_PROGRESS, (long) operations.get(0).getProgress(), true);
                                if (operations.get(0).getFinished() && operations.get(0).getId().equals(download.getId())) {
                                    mUrl = operations.get(0).getUrl();
                                    mId = operations.get(0).getId();
                                    break;
                                }
                            } else {
                                mNotificationUtils.showErrorNotification(getId().hashCode(), mFile.getName());
                                onError(operations.get(0).getError());
                                mFile.delete();
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        mNotificationUtils.showCanceledNotification(getId().hashCode(), mFile.getName());
                        mFile.delete();
                        break;
                    }
                } while (true);
            }
        } catch (Exception e) {
            if (e instanceof HttpException) {
                HttpException exception = (HttpException) e;
                onError(exception.response().errorBody());
            }
        }

    }

    private void onError(ResponseBody responseBody) {
        String errorMessage;
        String responseMessage = null;

        try {
            responseMessage = responseBody.string();
        } catch (Exception e) {
            sendBroadcastUnknownError(mId, mUrl, mFile.getName());
            mFile.delete();
            return;
        }

        if (responseMessage != null) {
            final JSONObject jsonObject = StringUtils.getJsonObject(responseMessage);
            if (jsonObject != null) {
                try {
                    errorMessage = jsonObject.getJSONObject(KEY_ERROR_INFO).getString(KEY_ERROR_INFO_MESSAGE);
                    sendBroadcastError(mId, mUrl, mFile.getName(), errorMessage);
                } catch (JSONException e) {
                    Log.e(TAG, "onErrorHandle()", e);
                    FirebaseUtils.addCrash(e);
                }
            }
            else {
                sendBroadcastUnknownError(mId, mUrl, mFile.getName());
                mFile.delete();
                return;
            }
        }
    }

    private void onError(String errorMessage) {
        switch (errorMessage) {
            case Api.Errors.EXCEED_FILE_SIZE_100:
                sendBroadcastError(mId, mUrl, mFile.getName(), mContext.getString(R.string.download_manager_exceed_size_100));
                break;
            case Api.Errors.EXCEED_FILE_SIZE_25:
                sendBroadcastError(mId, mUrl, mFile.getName(), mContext.getString(R.string.download_manager_exceed_size_25));
                break;
            default:
                sendBroadcastError(mId, mUrl, mFile.getName(), errorMessage);
                break;
        }
    }

    private void getArgs() {
        final Data data = getInputData();
        final Gson gson = new Gson();
        mUrl = StringUtils.getEncodedString(data.getString(URL_KEY));
        mId = data.getString(FILE_ID_KEY);
        mRequestDownload = gson.fromJson(data.getString(REQUEST_DOWNLOAD), RequestDownload.class);
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
    public static void sendBroadcastError(final String id, final String url, final String title, String error) {
        Intent intent = new Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title);
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ERROR, error);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }
}
