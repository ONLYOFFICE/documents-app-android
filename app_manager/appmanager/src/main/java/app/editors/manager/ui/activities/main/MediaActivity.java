package app.editors.manager.ui.activities.main;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.media.MediaPagerFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.UiUtils;

public class MediaActivity extends BaseAppActivity implements View.OnClickListener {

    public static final String TAG = MediaActivity.class.getSimpleName();
    public static final String TAG_MEDIA = "TAG_MEDIA";
    public static final String TAG_WEB_DAV = "TAG_WEB_DAV";

    public static final int ALPHA_DELAY = 300;
    public static final float ALPHA_FROM = 0.0f;
    public static final float ALPHA_TO = 1.0f;

    @BindView(R.id.app_bar_toolbar)
    protected Toolbar mAppBarToolbar;
    @BindView(R.id.app_bar_toolbar_container)
    protected FrameLayout mAppBarToolbarContainer;

    private Unbinder mUnbinder;
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;
    private ViewPropertyAnimator mViewPropertyAnimator;
    private Drawable mBackDrawable;

    private Runnable mToolbarRunnableGone = new Runnable() {
        @Override
        public void run() {
            if (mAppBarToolbar != null) {
                mAppBarToolbar.setVisibility(View.GONE);
            }
        }
    };

    private Runnable mToolbarRunnableVisible = new Runnable() {
        @Override
        public void run() {
            if (mAppBarToolbar != null) {
                mAppBarToolbar.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        mUnbinder = ButterKnife.bind(this);
        init(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppBarToolbar.removeCallbacks(mToolbarRunnableGone);
        mUnbinder.unbind();
        killSelf();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_bar_toolbar:
                showToolbar();
                break;
        }
    }

    private void init(final Bundle savedInstanceState) {
        initException();
        initToolbar();

        if (savedInstanceState == null) {
            final Explorer explorer = (Explorer) getIntent().getSerializableExtra(TAG_MEDIA);
            final boolean isWebDav = getIntent().getBooleanExtra(TAG_WEB_DAV, false);
            showFragment(MediaPagerFragment.newInstance(explorer, isWebDav), null);
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

    private void initToolbar() {
        mBackDrawable = UiUtils.getFilteredDrawable(this, R.drawable.ic_toolbar_back, R.color.colorWhite);
        setSupportActionBar(mAppBarToolbar);
        setStatusBarColor(R.color.colorBlack);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(mBackDrawable);
    }

    public void setToolbarView(final View view) {
        mAppBarToolbarContainer.removeAllViews();
        mAppBarToolbarContainer.addView(view);
    }

    public  void setToolbarState(final boolean isAnimating) {
        mAppBarToolbar.setOnClickListener(isAnimating? this : null);
        resetToolbarView(true);
    }

    public boolean showToolbar() {
        final boolean isVisible = mAppBarToolbar.getVisibility() == View.VISIBLE;
        resetToolbarView(isVisible);
        mViewPropertyAnimator = mAppBarToolbar.animate()
                .alpha(isVisible? ALPHA_FROM : ALPHA_TO)
                .setDuration(ALPHA_DELAY)
                .withEndAction(isVisible? mToolbarRunnableGone : mToolbarRunnableVisible);
        mViewPropertyAnimator.start();
        return isVisible;
    }

    private void resetToolbarView(final boolean isVisible) {
        if (mViewPropertyAnimator != null) {
            mViewPropertyAnimator.cancel();
        }

        mAppBarToolbar.removeCallbacks(mToolbarRunnableGone);
        mAppBarToolbar.removeCallbacks(mToolbarRunnableVisible);
        mAppBarToolbar.setVisibility(View.VISIBLE);
        mAppBarToolbar.setAlpha(isVisible? ALPHA_TO : ALPHA_FROM);
    }

    public boolean isToolbarVisible() {
        return mAppBarToolbar.getVisibility() == View.VISIBLE;
    }

    public static void show(final Fragment fragment, final Explorer explorer, final boolean isWebDAv) {
        final Intent intent = new Intent(fragment.getContext(), MediaActivity.class);
        intent.putExtra(TAG_MEDIA, explorer);
        intent.putExtra(TAG_WEB_DAV, isWebDAv);
        fragment.startActivityForResult(intent, REQUEST_ACTIVITY_MEDIA);
    }

}
