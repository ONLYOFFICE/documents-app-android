package app.editors.manager.managers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import app.editors.manager.R;
import app.editors.manager.managers.utils.FirebaseUtils;


public class DownloadReceiver extends BaseReceiver<Intent> {

    public static final String DOWNLOAD_ACTION_ERROR = "DOWNLOAD_ACTION_ERROR";
    public static final String DOWNLOAD_ACTION_ERROR_FREE_SPACE = "DOWNLOAD_ACTION_ERROR_FREE_SPACE";
    public static final String DOWNLOAD_ACTION_ERROR_URL_INIT = "DOWNLOAD_ACTION_ERROR_URL_INIT";
    public static final String DOWNLOAD_ACTION_PROGRESS = "DOWNLOAD_ACTION_PROGRESS";
    public static final String DOWNLOAD_ACTION_COMPLETE = "DOWNLOAD_ACTION_COMPLETE";
    public static final String DOWNLOAD_ACTION_REPEAT = "DOWNLOAD_ACTION_REPEAT";
    public static final String DOWNLOAD_ACTION_CANCELED = "DOWNLOAD_ACTION_CANCELED";

    public static final String EXTRAS_KEY_ID = "EXTRAS_KEY_ID";
    public static final String EXTRAS_KEY_URL = "EXTRAS_KEY_URL";
    public static final String EXTRAS_KEY_TITLE = "EXTRAS_KEY_TITLE";
    public static final String EXTRAS_KEY_TOTAL = "EXTRAS_KEY_TOTAL";
    public static final String EXTRAS_KEY_PATH = "EXTRAS_KEY_PATH";
    public static final String EXTRAS_KEY_MIME_TYPE = "EXTRAS_KEY_MIME_TYPE";
    public static final String EXTRAS_KEY_PROGRESS = "EXTRAS_KEY_PROGRESS";
    public static final String EXTRAS_KEY_CANCELED = "EXTRAS_KEY_CANCELED";
    public static final String EXTRAS_KEY_ERROR = "EXTRAS_KEY_ERROR";

    public static final int EXTRAS_VALUE_CANCELED = 0;
    public static final int EXTRAS_VALUE_CANCELED_NOT_FOUND = 1;

    public interface OnDownloadListener {
        void onDownloadError(String id, String url, String title, String info);
        void onDownloadProgress(String id, int total, int progress);
        void onDownloadComplete(String id, String url, String title, String info, String path, String mime);
        void onDownloadCanceled(String id, String info);
        void onDownloadRepeat(String id, String title, String info);
    }

    private OnDownloadListener mOnDownloadListener;

    public DownloadReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (mOnDownloadListener != null) {
                final String action = intent.getAction();
                switch (action) {
                    case DOWNLOAD_ACTION_ERROR: {
                        final String id = intent.getStringExtra(EXTRAS_KEY_ID);
                        final String url = intent.getStringExtra(EXTRAS_KEY_URL);
                        final String title = intent.getStringExtra(EXTRAS_KEY_TITLE);
                        final String info = intent.getStringExtra(EXTRAS_KEY_ERROR);//context.getString(R.string.download_manager_error);
                        if(info != null) {
                            mOnDownloadListener.onDownloadError(id, url, title, info);
                        } else {
                            mOnDownloadListener.onDownloadError(id, url, title, context.getString(R.string.download_manager_error));
                        }
                        break;
                    }

                    case DOWNLOAD_ACTION_ERROR_FREE_SPACE: {
                        final String info = context.getString(R.string.download_manager_error_free_space);
                        mOnDownloadListener.onDownloadError(info, "", "", "");
                        break;
                    }

                    case DOWNLOAD_ACTION_ERROR_URL_INIT: {
                        final String id = intent.getStringExtra(EXTRAS_KEY_ID);
                        final String url = intent.getStringExtra(EXTRAS_KEY_URL);
                        final String title = intent.getStringExtra(EXTRAS_KEY_TITLE);
                        final String info = context.getString(R.string.download_manager_error_url);
                        mOnDownloadListener.onDownloadError(info, id, url, title);
                        break;
                    }

                    case DOWNLOAD_ACTION_PROGRESS: {
                        final String id = intent.getStringExtra(EXTRAS_KEY_ID);
                        final int total = intent.getIntExtra(EXTRAS_KEY_TOTAL, 0);
                        final int progress = intent.getIntExtra(EXTRAS_KEY_PROGRESS, 0);
                        mOnDownloadListener.onDownloadProgress(id, total, progress);
                        break;
                    }

                    case DOWNLOAD_ACTION_COMPLETE: {
                        final String id = intent.getStringExtra(EXTRAS_KEY_ID);
                        final String url = intent.getStringExtra(EXTRAS_KEY_URL);
                        final String title = intent.getStringExtra(EXTRAS_KEY_TITLE);
                        final String path = intent.getStringExtra(EXTRAS_KEY_PATH);
                        final String mime = intent.getStringExtra(EXTRAS_KEY_MIME_TYPE);
                        final String info = context.getString(R.string.download_manager_complete);
                        mOnDownloadListener.onDownloadComplete(id, url, title, info, path, mime);
                        break;
                    }

                    case DOWNLOAD_ACTION_REPEAT: {
                        final String id = intent.getStringExtra(EXTRAS_KEY_ID);
                        final String title = intent.getStringExtra(EXTRAS_KEY_TITLE);
                        final String info = context.getString(R.string.download_manager_repeat);
                        mOnDownloadListener.onDownloadRepeat(id, title, info);
                        break;
                    }

                    case DOWNLOAD_ACTION_CANCELED: {
                        final String id = intent.getStringExtra(EXTRAS_KEY_ID);
                        final String info = context.getString(R.string.download_manager_cancel);
                        final int value = intent.getIntExtra(EXTRAS_KEY_CANCELED, EXTRAS_VALUE_CANCELED);
                        mOnDownloadListener.onDownloadCanceled(id, info);
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
        intentFilter.addAction(DOWNLOAD_ACTION_ERROR);
        intentFilter.addAction(DOWNLOAD_ACTION_ERROR_URL_INIT);
        intentFilter.addAction(DOWNLOAD_ACTION_PROGRESS);
        intentFilter.addAction(DOWNLOAD_ACTION_COMPLETE);
        intentFilter.addAction(DOWNLOAD_ACTION_REPEAT);
        intentFilter.addAction(DOWNLOAD_ACTION_CANCELED);
        return intentFilter;
    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        mOnDownloadListener = onDownloadListener;
    }

}
