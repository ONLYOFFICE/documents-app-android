package app.editors.manager.managers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.Nullable;

import app.editors.manager.R;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.mvp.models.explorer.CloudFile;

public class UploadReceiver extends BaseReceiver<Intent> {

    public static final String UPLOAD_ACTION_ERROR = "UPLOAD_ACTION_ERROR";
    public static final String UPLOAD_ACTION_ERROR_URL_INIT = "UPLOAD_ACTION_ERROR_URL_INIT";
    public static final String UPLOAD_ACTION_PROGRESS = "UPLOAD_ACTION_PROGRESS";
    public static final String UPLOAD_ACTION_COMPLETE = "UPLOAD_ACTION_COMPLETE";
    public static final String UPLOAD_ACTION_REPEAT = "UPLOAD_ACTION_REPEAT";
    public static final String UPLOAD_ACTION_CANCELED = "UPLOAD_ACTION_CANCELED";
    public static final String UPLOAD_AND_OPEN = "UPLOAD_AND_OPEN";

    public static final String EXTRAS_KEY_PATH = "EXTRAS_KEY_PATH";
    public static final String EXTRAS_KEY_TITLE = "EXTRAS_KEY_TITLE";
    public static final String EXTRAS_KEY_FILE = "EXTRAS_KEY_FILE";
    public static final String EXTRAS_KEY_PROGRESS = "EXTRAS_KEY_PROGRESS";
    public static final String EXTRAS_KEY_ID = "EXTRAS_KEY_ID";
    public static final String EXTRAS_FOLDER_ID = "EXTRAS_FOLDER_ID";


    public interface OnUploadListener {
        void onUploadError(@Nullable String path, String info, String file);

        void onUploadComplete(String path, String info, @Nullable String title, CloudFile file, String id);

        void onUploadAndOpen(String path, @Nullable String title, CloudFile file, String id);

        void onUploadFileProgress(int progress, String id, String folderId);

        void onUploadCanceled(String path, String info, String id);

        void onUploadRepeat(String path, String info);
    }

    private OnUploadListener mOnUploadListener;

    public UploadReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (mOnUploadListener != null) {
                final String action = intent.getAction();
                switch (action) {
                    case UPLOAD_ACTION_ERROR: {
                        final String title = intent.getStringExtra(EXTRAS_KEY_TITLE);
                        final String file = intent.getStringExtra(EXTRAS_KEY_FILE);
                        final String info = context.getString(R.string.upload_manager_error);
                        mOnUploadListener.onUploadError(title, info, file);
                        break;
                    }

                    case UPLOAD_ACTION_ERROR_URL_INIT: {
                        final String title = intent.getStringExtra(EXTRAS_KEY_TITLE);
                        final String info = context.getString(R.string.upload_manager_error_url);
                        mOnUploadListener.onUploadError(title, info, null);
                        break;
                    }

                    case UPLOAD_ACTION_PROGRESS: {
                        final String file = intent.getStringExtra(EXTRAS_KEY_FILE);
                        final String folder = intent.getStringExtra(EXTRAS_FOLDER_ID);
                        final int progress = intent.getIntExtra(EXTRAS_KEY_PROGRESS, 0);
                        mOnUploadListener.onUploadFileProgress(progress, file, folder);
                        break;
                    }

                    case UPLOAD_ACTION_COMPLETE: {
                        final String path = intent.getStringExtra(EXTRAS_KEY_PATH);
                        final String title = intent.getStringExtra(EXTRAS_KEY_TITLE);
                        final CloudFile file = (CloudFile) intent.getSerializableExtra(EXTRAS_KEY_FILE);
                        final String id = intent.getStringExtra(EXTRAS_KEY_ID);
                        final String info = context.getString(R.string.upload_manager_complete);
                        mOnUploadListener.onUploadComplete(path, info, title, file, id);
                        break;
                    }

                    case UPLOAD_AND_OPEN: {
                        final String path = intent.getStringExtra(EXTRAS_KEY_PATH);
                        final String title = intent.getStringExtra(EXTRAS_KEY_TITLE);
                        final CloudFile file = (CloudFile) intent.getSerializableExtra(EXTRAS_KEY_FILE);
                        final String id = intent.getStringExtra(EXTRAS_KEY_ID);
                        mOnUploadListener.onUploadAndOpen(path, title, file, id);
                        break;
                    }

                    case UPLOAD_ACTION_REPEAT: {
                        final String path = intent.getStringExtra(EXTRAS_KEY_PATH);
                        final String info = context.getString(R.string.upload_manager_repeat);
                        mOnUploadListener.onUploadRepeat(path, info);
                        break;
                    }

                    case UPLOAD_ACTION_CANCELED: {
                        final String path = intent.getStringExtra(EXTRAS_KEY_PATH);
                        final String id = intent.getStringExtra(EXTRAS_KEY_ID);
                        final String info = context.getString(R.string.upload_manager_cancel);
                        mOnUploadListener.onUploadCanceled(path, info, id);
                        break;
                    }
                }
            }
        } catch (RuntimeException e) {
            // No need handle
            FirebaseUtils.addCrash(e);
        }
    }

    @Override
    public IntentFilter getFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPLOAD_ACTION_ERROR);
        intentFilter.addAction(UPLOAD_ACTION_ERROR_URL_INIT);
        intentFilter.addAction(UPLOAD_ACTION_PROGRESS);
        intentFilter.addAction(UPLOAD_ACTION_COMPLETE);
        intentFilter.addAction(UPLOAD_ACTION_REPEAT);
        intentFilter.addAction(UPLOAD_ACTION_CANCELED);
        intentFilter.addAction(UPLOAD_AND_OPEN);
        return intentFilter;
    }

    public void setOnUploadListener(OnUploadListener onUploadListener) {
        mOnUploadListener = onUploadListener;
    }

}
