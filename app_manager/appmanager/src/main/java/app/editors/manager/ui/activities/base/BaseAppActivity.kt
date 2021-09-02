package app.editors.manager.ui.activities.base;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import app.editors.manager.R;
import app.editors.manager.ui.interfaces.ContextDialogInterface;
import lib.toolkit.base.managers.utils.ActivitiesUtils;
import lib.toolkit.base.managers.utils.FragmentUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.activities.base.BaseActivity;

public abstract class BaseAppActivity extends BaseActivity implements FragmentManager.OnBackStackChangedListener, ContextDialogInterface {

    private final String TAG = getClass().getSimpleName();
    private static final String TAG_FINISH = "TAG_FINISH";
    private static final int TIMER_FINISH = 5000;

    protected Handler mHandler;
    protected Runnable mFinishRunnable;
    protected boolean mIsFinish;
    protected boolean mIsBackStackNotice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TAG_FINISH, mIsFinish);
    }

    @Override
    public void onContextDialogOpen() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mFinishRunnable);
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }

    @Override
    public void onBackStackChanged() {

    }

    private void init(final Bundle savedInstanceState) {
        initHandlers();
        initViews();
    }

    private void initHandlers() {
        mIsBackStackNotice = false;
        mHandler = new Handler();
        mFinishRunnable = () -> mIsFinish = false;
    }

    private void startResetFinishTimer() {
        mHandler.postDelayed(mFinishRunnable, TIMER_FINISH);
    }

    private void initViews() {
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        mIsFinish = false;
    }

    /*
     * Fragment operations
     * */
    protected void showFragment(@NonNull final Fragment fragment, @Nullable final String tag) {
        FragmentUtils.showFragment(getSupportFragmentManager(), fragment, R.id.frame_container, tag, false);
    }

    protected void showSingleFragment(@NonNull final Fragment fragment, @Nullable final String backStackTag) {
        FragmentUtils.showSingleFragment(getSupportFragmentManager(), fragment, R.id.frame_container, backStackTag);
    }

    /*
     * Helper methods
     * */
    protected void showUrlInBrowser(final String url) {
        ActivitiesUtils.showBrowser(this, getString(R.string.chooser_web_browser), url);
    }

    protected void showEmailClients(final String to, final String subject, final String body) {
        ActivitiesUtils.showEmail(this, getString(R.string.chooser_email_client), to, subject, body);
    }

    protected void showEmailClientTemplate() {
        showEmailClients(getString(R.string.app_support_email), getString(R.string.about_email_subject),
                UiUtils.getDeviceInfoString(null, true));
    }

    protected void showEmailClientTemplate(@NonNull final String message) {
        showEmailClients(getString(R.string.app_support_email), getString(R.string.about_email_subject),
                message + UiUtils.getDeviceInfoString(null,false));
    }
}
