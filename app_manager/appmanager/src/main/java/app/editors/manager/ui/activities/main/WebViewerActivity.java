package app.editors.manager.ui.activities.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.main.WebViewerFragment;


public class WebViewerActivity extends BaseAppActivity {

    public static final String TAG = WebViewerActivity.class.getSimpleName();
    public static final String TAG_VIEWER_FAIL = "TAG_VIEWER_FAIL";
    private static final String TAG_FILE = "TAG_FILE";

    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer_web);
        init(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killSelf();
    }

    private void init(final Bundle savedInstanceState) {
        initException();
        if (savedInstanceState == null) {
            final File file = (File) getIntent().getSerializableExtra(TAG_FILE);
            showFragment(WebViewerFragment.newInstance(file), null);
        }
    }

    private void initException() {
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Log.d(TAG, "ID: " + t.getId() + "; NAME: " + t.getName());
            final Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
            mUncaughtExceptionHandler.uncaughtException(t, e);
        });
    }

    private static Intent getActivityIntent(final Context context) {
        final Intent intent = new Intent(context, WebViewerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        return intent;
    }

    public static void show(final Fragment fragment, final File file) {
        final Intent intent = getActivityIntent(fragment.getContext());
        intent.putExtra(WebViewerActivity.TAG_FILE, file);
        fragment.startActivityForResult(intent, REQUEST_ACTIVITY_WEB_VIEWER);
    }

    public static void show(final Activity activity, final File file) {
        final Intent intent = getActivityIntent(activity);
        intent.putExtra(WebViewerActivity.TAG_FILE, file);
        activity.startActivityForResult(intent, REQUEST_ACTIVITY_WEB_VIEWER);
    }

}
