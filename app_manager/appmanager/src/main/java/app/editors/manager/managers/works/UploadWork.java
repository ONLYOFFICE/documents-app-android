package app.editors.manager.managers.works;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import app.editors.manager.app.App;
import app.editors.manager.managers.receivers.UploadReceiver;
import app.editors.manager.managers.retrofit.ProgressRequestBody;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.managers.utils.NewNotificationUtils;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.mvp.models.response.ResponseFile;
import lib.toolkit.base.managers.utils.ContentResolverUtils;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.NetworkUtils;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Response;

public class UploadWork extends Worker {

    public static final String TAG = UploadWork.class.getSimpleName();

    private final String mToken;
    private final NewNotificationUtils mNotificationUtils;

    public static final String TAG_UPLOAD_FILES = "TAG_UPLOAD_FILES";
    public static final String TAG_FOLDER_ID = "TAG_FOLDER_ID";

    public static final String ACTION_UPLOAD = "ACTION_UPLOAD";
    public static final String ACTION_UPLOAD_MY = "ACTION_UPLOAD_MY";

    private static final String HEADER_NAME = "Content-Disposition";

    private String mAction;
    final private Context mContext;

    private String mPath;
    private String mFolderId;
    private String mTitle;
    private Uri mFrom;

    private long mTimeMark = 0L;

    Call<ResponseFile> mCall;

    @Inject
    public RetrofitTool mRetrofitTool;
    @Inject
    public PreferenceTool mPreferenceTool;

    private static final Map<String, ArrayList<UploadFile>> mMapUploadFiles =
            Collections.synchronizedMap(new HashMap<>());

    public UploadWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mRetrofitTool = App.getApp().getAppComponent().getRetrofit();
        mToken = App.getApp().getAppComponent().getPreference().getToken();
        mNotificationUtils = new NewNotificationUtils(getApplicationContext(), TAG);
        mContext = getApplicationContext();
    }

    @NonNull
    @Override
    public Result doWork() {
        getArgs();
        ResponseFile responseFile;
        mPath = mFrom.getPath();
        mTitle = ContentResolverUtils.getName(getApplicationContext(), mFrom);

        if(mAction.equals(ACTION_UPLOAD_MY)) {
            mCall = mRetrofitTool.getApiWithPreferences().uploadFileToMy(mToken, createMultipartBody(mFrom));
        } else {
            mCall = mRetrofitTool.getApiWithPreferences().uploadFile(mToken, mFolderId, createMultipartBody(mFrom));
        }
        try {
            Response<ResponseFile> response = mCall.execute();
            if(response.isSuccessful() && response.body() != null) {
                responseFile = response.body();
                mNotificationUtils.removeNotification(getId().hashCode());
                mNotificationUtils.showUploadCompleteNotification(getId().hashCode(), mTitle);
                sendBroadcastUploadComplete(mPath, mTitle, responseFile.getResponse(), mPath);
                removeUploadFile(mFrom);
            }
        } catch (IOException e) {
            if(isStopped()) {
                mNotificationUtils.showCanceledUploadNotification(getId().hashCode(), mTitle);
                sendBroadcastUploadCanceled(mPath);
                removeUploadFile(mFrom);
            } else {
                mNotificationUtils.showUploadErrorNotification(getId().hashCode(), mTitle);
                sendBroadcastUnknownError(mTitle, mPath);
                if (!NetworkUtils.isOnline(mContext)) {
                    return Result.retry();
                } else {
                    removeUploadFile(mFrom);
                }
            }
        }
        return Result.success();
    }
    private void getArgs() {
        final Data data = getInputData();
        mAction = data.getString(UploadWork.ACTION_UPLOAD_MY);
        mFrom = Uri.parse(data.getString(UploadWork.TAG_UPLOAD_FILES));
        mFolderId = data.getString(UploadWork.TAG_FOLDER_ID);
    }

    private MultipartBody.Part createMultipartBody(Uri uri) {
        return MultipartBody.Part.create(getHeaders(), createRequestBody(uri));
    }

    private Headers getHeaders() {
        return new Headers.Builder()
                .addUnsafeNonAscii(HEADER_NAME, "form-data; name=" + mTitle + "; filename=" + mTitle)
                .build();
    }

    private ProgressRequestBody createRequestBody(Uri uri) {
        ProgressRequestBody requestBody = new ProgressRequestBody(App.getApp(), uri);
        requestBody.setOnUploadCallbacks(((total, progress) -> {
            if(!isStopped()) {
                showProgress(total, progress);
            } else {
                mCall.cancel();
            }
        }));
        return requestBody;
    }

    private void removeUploadFile(Uri completeFile) {
        ArrayList<UploadFile> files = getUploadFiles(mFolderId);
        if (files == null || files.isEmpty()) {
            return;
        }
        for (UploadFile file : files) {
            if (file.getId().equals(completeFile.getPath())) {
                files.remove(file);
                break;
            }
        }
        putUploadFiles(mFolderId, files);
    }

    private void showProgress(Long total, Long progress) {
        final long deltaTime = System.currentTimeMillis() - mTimeMark;
        if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
            mTimeMark = System.currentTimeMillis();
            final int percent = FileUtils.getPercentOfLoading(total, progress);
            final int id = getId().hashCode();
            final String tag = getId().toString();
            mNotificationUtils.showUploadProgressNotification(id, tag, mTitle, percent);
            sendBroadcastProgress(percent, mPath, mFolderId);
        }
    }
    public static ArrayList<UploadFile> getUploadFiles(String id) {
        return mMapUploadFiles.get(id);
    }

    private void putUploadFiles(String id, ArrayList<UploadFile> uploadFiles) {
        mMapUploadFiles.put(id, uploadFiles);
    }

    public static void putNewUploadFiles(String id, ArrayList<UploadFile> uploadFiles) {
        ArrayList<UploadFile> oldFiles = mMapUploadFiles.get(id);
        if (oldFiles != null) {
            uploadFiles.removeAll(oldFiles);
            oldFiles.addAll(uploadFiles);
            mMapUploadFiles.put(id, oldFiles);
        } else {
            mMapUploadFiles.put(id, uploadFiles);
        }
    }

    private void sendBroadcastUnknownError(final String title, final String uploadFile) {
        Intent intent = new Intent(UploadReceiver.UPLOAD_ACTION_ERROR);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, uploadFile);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_TITLE, title);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    private void sendBroadcastUploadComplete(final String path, final String title, final File file, final String id) {
        Intent intent = new Intent(UploadReceiver.UPLOAD_ACTION_COMPLETE);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PATH, path);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_TITLE, title);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, file);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    private void sendBroadcastUploadCanceled(final String path) {
        Intent intent = new Intent(UploadReceiver.UPLOAD_ACTION_CANCELED);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PATH, path);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_ID, path);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    private void sendBroadcastProgress(final int progress, final String file, String folderId) {
        Intent intent = new Intent(UploadReceiver.UPLOAD_ACTION_PROGRESS);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, file);
        intent.putExtra(UploadReceiver.EXTRAS_FOLDER_ID, folderId);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PROGRESS, progress);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }
}
