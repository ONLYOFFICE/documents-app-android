/*
 * Created by Michael Efremov on 26.10.20 13:37
 */

package app.editors.manager.managers.works;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.IOException;

import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.managers.utils.NewNotificationUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class DownloadWork extends Worker {

    private final String TAG = DownloadWork.class.getSimpleName();

    private static String PATH_DOWNLOAD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OnlyOffice";

    public static String URL_KEY = "URL_KEY";
    public static String FILE_NAME_KEY = "FILE_NAME_KEY";
    public static String FILE_ID_KEY = "FILE_ID_KEY";

    private RetrofitTool mRetrofitTool;
    private String mToken;
    private NewNotificationUtils mNotificationUtils;
    private String mUrl;
    private String mFileName;
    private String mId;
    private Long mTimeMark = 0L;

    public DownloadWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mRetrofitTool = App.getApp().getAppComponent().getRetrofit();
        mToken = App.getApp().getAppComponent().getPreference().getToken();
        mNotificationUtils = new NewNotificationUtils(getApplicationContext(), TAG);
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

        final File file = new File(PATH_DOWNLOAD, mFileName);

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
                FileUtils.writeFromResponseBody(response.body(), file, (total, progress) -> {
                    showProgress(total, progress);
                    return isStopped();
                }, () -> {
                    mNotificationUtils.removeNotification(getId().hashCode());
                    mNotificationUtils.showCompleteNotification(file, getId().hashCode(), mFileName);
                }, message -> {
                    mNotificationUtils.removeNotification(getId().hashCode());
                    FileUtils.deletePath(file.getAbsolutePath());
                    if (isStopped()) {
                        mNotificationUtils.showCanceledNotification(getId().hashCode(), mFileName);
                    } else {
                        mNotificationUtils.showErrorNotification(getId().hashCode(), mFileName);
                    }
                });
            }
        } catch (IOException e) {
            FileUtils.deletePath(file.getAbsolutePath());
            mNotificationUtils.showErrorNotification(getId().hashCode(), mFileName);
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
            mNotificationUtils.showProgressNotification(id, tag, mFileName, percent);
        }
    }

    private void getArgs() {
        final Data data = getInputData();
        mUrl = StringUtils.getEncodedString(data.getString(URL_KEY));
        mFileName = data.getString(FILE_NAME_KEY);
        mId = data.getString(FILE_ID_KEY);
    }
}
