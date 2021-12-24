package app.editors.manager.managers.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.receivers.UploadReceiver;
import app.editors.manager.managers.retrofit.ProgressRequestBody;
import app.editors.manager.managers.utils.NotificationUtils;
import app.editors.manager.mvp.models.explorer.CloudFile;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.mvp.models.response.ResponseFile;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import lib.toolkit.base.managers.utils.ContentResolverUtils;
import lib.toolkit.base.managers.utils.FileUtils;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

@Deprecated
public class UploadService extends Service {

    public static final String TAG = UploadService.class.getSimpleName();
    public static final String TAG_URI = "TAG_URI";
    public static final String TAG_UPLOAD_FILES = "TAG_UPLOAD_FILES";
    public static final String TAG_FOLDER_ID = "TAG_FOLDER_ID";
    public static final String TAG_ID = "TAG_ID";

    public static final String ACTION_UPLOAD = "ACTION_UPLOAD";
    public static final String ACTION_CANCEL = "ACTION_CANCEL";
    public static final String ACTION_UPLOAD_MY = "ACTION_UPLOAD_MY";

    private static final String HEADER_NAME = "Content-Disposition";

    private ResponseFile mResponseFile;

    private String mAction;

    private String mPath;
    private String mFolderId;
    private String mTitle;

    private long mTimeMark;


    private NotificationUtils mNotificationUtils;

    private LinkedList<UploadFile> mLinkedList;

    private CompositeDisposable mDisposable = new CompositeDisposable();
    private static Map<String, ArrayList<UploadFile>> mMapUploadFiles =
            Collections.synchronizedMap(new HashMap<>());

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    @Override
    public void onCreate() {
        mNotificationUtils = new NotificationUtils(App.getApp(), TAG);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mDisposable.dispose();
        mMapUploadFiles.clear();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_UPLOAD_MY:
                    mAction = ACTION_UPLOAD_MY;
                    mLinkedList = new LinkedList<>();
                    mLinkedList.add(intent.getParcelableExtra(TAG_UPLOAD_FILES));
                    uploadToMy(intent.getParcelableExtra(TAG_UPLOAD_FILES));
                    break;
                case ACTION_UPLOAD:
                    if (mLinkedList != null) {
                        ArrayList<UploadFile> newList = intent.getParcelableArrayListExtra(TAG_UPLOAD_FILES);
                        newList.removeAll(mLinkedList);
                        mLinkedList.addAll(newList);
                    } else {
                        mLinkedList = new LinkedList<>(intent.getParcelableArrayListExtra(TAG_UPLOAD_FILES));
                        upload();
                    }
                    break;
                case ACTION_CANCEL:
                    if (mLinkedList != null) {
                        cancelUpload(intent);
                    }
                    break;
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void cancelUpload(Intent intent) {
        final String id = intent.getStringExtra(TAG_ID);
        UploadFile deleteFile = searchDeleteFile(id);
        mNotificationUtils.removeNotification(mPath, mPath.hashCode());

        if (mDisposable.size() != 0 && deleteFile != null) {
            removeUploadFile(deleteFile);
            mLinkedList.remove(deleteFile);
            sendBroadcastUploadCanceled(mPath, deleteFile.getId());
        } else {
            sendBroadcastUploadCanceled(mPath, id);
        }

        if (mLinkedList.size() != 0) {
            mDisposable.clear();
            upload();
        } else {
            stopSelf();
        }
    }

    private UploadFile searchDeleteFile(String id) {
        if (mLinkedList != null && mLinkedList.size() != 0) {
            for (UploadFile file : mLinkedList) {
                if (file.getId().equals(id)) {
                    return file;
                }
            }
        }
        return mLinkedList != null ? mLinkedList.pollFirst() : null;
    }

    private void upload() {
        Uri uri = mLinkedList.getFirst().getUri();
        mPath = uri.getPath();
        mFolderId = mLinkedList.getFirst().getFolderId();
        mTitle = ContentResolverUtils.getName(getApplicationContext(), uri);
        try {
            mDisposable.add(uploadFile(uri)
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::showProgress,
                            throwable -> sendError(),
                            this::completeUpload)
            );
        } catch (RuntimeException e) {
            sendBroadcastUnknownError(mPath, mLinkedList.getFirst());
        }
    }

    private void uploadToMy(UploadFile uploadFile) {
        Uri uri = uploadFile.getUri();
        mPath = uri.getPath();
        mTitle = ContentResolverUtils.getName(getApplicationContext(), uri);
        try {
            mDisposable.add(uploadFileToMy(uri)
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::showProgress,
                            throwable -> sendError(),
                            this::completeUpload)
            );
        } catch (RuntimeException e) {
            sendBroadcastUnknownError(mPath, uploadFile);
        }
    }

    public Flowable<Integer> uploadFileToMy(Uri uri) {
        return Flowable.create(emitter -> {
            try {
//                mResponseFile = mRetrofitTool
//                        .getApiWithPreferences()
//                        .uploadFileToMy(mPreferenceTool.getToken(), createMultipartBody(uri, emitter))
//                        .blockingGet();
                emitter.onComplete();
            } catch (Exception e) {
                emitter.tryOnError(e);
            }
        }, BackpressureStrategy.LATEST);
    }

    public Flowable<Integer> uploadFile(Uri uri) {
        return Flowable.create(emitter -> {
            try {
//                mResponseFile = mRetrofitTool
//                        .getApiWithPreferences()
//                        .uploadFile(mPreferenceTool.getToken(), mFolderId, createMultipartBody(uri, emitter))
//                        .blockingGet();
                emitter.onComplete();
            } catch (Exception e) {
                emitter.tryOnError(e);
            }
        }, BackpressureStrategy.LATEST);
    }

    private MultipartBody.Part createMultipartBody(Uri uri, FlowableEmitter<Integer> emitter) {
        return MultipartBody.Part.create(getHeaders(), createRequestBody(uri, emitter));
    }

    private Headers getHeaders() {
        return new Headers.Builder()
                .addUnsafeNonAscii(HEADER_NAME, "form-data; name=" + mTitle + "; filename=" + mTitle)
                .build();
    }

    private RequestBody createRequestBody(Uri uri, FlowableEmitter<Integer> emitter) {
        ProgressRequestBody requestBody = new ProgressRequestBody(App.getApp(), uri);
        requestBody.setOnUploadCallbacks((total, progress) -> {
            long deltaTime = System.currentTimeMillis() - mTimeMark;
            if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
                emitter.onNext(getPercent(total, progress));
            }
            return Unit.INSTANCE;
        });
        return requestBody;
    }

    private Integer getPercent(long total, long progress) {
        mTimeMark = System.currentTimeMillis();
        return FileUtils.getPercentOfLoading(total, progress);
    }

    private void completeUpload() {
        UploadFile completeFile = mLinkedList.removeFirst();
        removeUploadFile(completeFile);
        if (mAction != null && mAction.equals(ACTION_UPLOAD_MY)) {
            mAction = null;
            sendBroadcastUploadMyComplete(mPath, mTitle, mResponseFile.getResponse(), completeFile.getId());
        } else {
            sendBroadcastUploadComplete(mPath, mTitle, mResponseFile.getResponse(), completeFile.getId());
        }
        mNotificationUtils.removeNotification(mPath, mPath.hashCode());
        if (mLinkedList.size() != 0) {
            upload();
        } else {
            stopSelf();
        }
    }

    private void removeUploadFile(UploadFile completeFile) {
        ArrayList<UploadFile> files = getUploadFiles(completeFile.getFolderId());
        if (files == null || files.isEmpty()) {
            return;
        }
        for (UploadFile file : files) {
            if (file.getId().equals(completeFile.getId())) {
                completeFile = file;
                break;
            }
        }
        files.remove(completeFile);
        putUploadFiles(completeFile.getFolderId(), files);
    }

    private void showProgress(int progress) {
        mNotificationUtils.showForegroundNotification(mPath, mTitle, getString(R.string.upload_manager_progress_title),
                FileUtils.LOAD_MAX_PROGRESS, progress, this);
        sendBroadcastProgress(progress, mLinkedList.getFirst());
    }

    private void sendError() {
        sendBroadcastUnknownError(mTitle, mLinkedList.removeFirst());
        mNotificationUtils.removeNotification(mPath, mPath.hashCode());
        if (mLinkedList.size() != 0) {
            upload();
        } else {
            stopSelf();
        }
    }

    /*
     * Actions for service
     * */
    public static void startUpload(final String folderId, ArrayList<UploadFile> uploadFiles) {
        final Intent intent = new Intent(App.getApp(), UploadService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(TAG_UPLOAD_FILES, uploadFiles);
        intent.putExtra(TAG_FOLDER_ID, folderId);
        App.getApp().startService(intent);
    }

    public static void startUploadToMy(final UploadFile uploadFile) {
        final Intent intent = new Intent(App.getApp(), UploadService.class);
        intent.setAction(ACTION_UPLOAD_MY);
        intent.putExtra(TAG_UPLOAD_FILES, uploadFile);
        App.getApp().startService(intent);
    }

    public static void cancelUpload(final Uri uri, String id) {
        final Intent intent = new Intent(App.getApp(), UploadService.class);
        intent.setAction(ACTION_CANCEL);
        intent.putExtra(TAG_ID, id);
        intent.putExtra(TAG_URI, uri);
        App.getApp().startService(intent);
    }

    /*
     * Broadcasts callbacks
     * */
    private void sendBroadcastUnknownError(final String title, final UploadFile uploadFile) {
        Intent intent = new Intent(UploadReceiver.UPLOAD_ACTION_ERROR);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, uploadFile);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_TITLE, title);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    private void sendBroadcastUploadComplete(final String path, final String title, final CloudFile file, final String id) {
        Intent intent = new Intent(UploadReceiver.UPLOAD_ACTION_COMPLETE);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PATH, path);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_TITLE, title);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, file);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    private void sendBroadcastUploadMyComplete(final String path, final String title, final CloudFile file, final String id) {
        Intent intent = new Intent(UploadReceiver.UPLOAD_ACTION_COMPLETE);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PATH, path);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_ID, id);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_TITLE, title);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, file);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    private void sendBroadcastUploadCanceled(final String path, final String id) {
        Intent intent = new Intent(UploadReceiver.UPLOAD_ACTION_CANCELED);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PATH, path);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_ID, id);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }

    private void sendBroadcastProgress(final int progress, final UploadFile file) {
        Intent intent = new Intent(UploadReceiver.UPLOAD_ACTION_PROGRESS);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, file);
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PROGRESS, progress);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent);
    }
}
