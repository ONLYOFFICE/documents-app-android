package app.editors.manager.ui.activities.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.share.SettingsFragment;
import app.editors.manager.ui.views.animation.HeightValueAnimator;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ShareActivity extends BaseAppActivity {

    public static final String TAG = ShareActivity.class.getSimpleName();
    public static final String TAG_SHARE_ITEM = "TAG_SHARE_ITEM";
    public static final String TAG_RESULT = "TAG_RESULT";

    private static final String PIXEL_C = "Pixel C";

    protected Unbinder mUnbinder;
    @BindView(R.id.app_layout)
    protected CoordinatorLayout mAppLayout;
    @BindView(R.id.app_bar_layout)
    protected AppBarLayout mAppBarLayout;
    @BindView(R.id.app_bar_toolbar)
    protected Toolbar mAppBarToolbar;
    @BindView(R.id.app_bar_tabs)
    protected TabLayout mTabLayout;

    private HeightValueAnimator mHeightValueAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        mUnbinder = ButterKnife.bind(this);
        setFinishOnTouchOutside(true);
        init(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (Build.MODEL.equals(PIXEL_C)) {
            super.onConfigurationChanged(newConfig);
        } else {
            recreate();
            super.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHeightValueAnimator.clear();
        mUnbinder.unbind();
    }

    private void init(final Bundle savedInstanceState) {
        setSupportActionBar(mAppBarToolbar);
        mHeightValueAnimator = new HeightValueAnimator(mAppBarToolbar);

        if (savedInstanceState == null) {
            final Item item = (Item) getIntent().getSerializableExtra(TAG_SHARE_ITEM);
            showFragment(SettingsFragment.newInstance(item), null);
        }
    }

    public void expandAppBar() {
        mHeightValueAnimator.animate(true);
    }

    public void collapseAppBar() {
        mHeightValueAnimator.animate(false);
    }

    public TabLayout getTabLayout() {
        return mTabLayout;
    }

    public static void show(final Fragment fragment, final Item item) {
        final Intent intent = new Intent(fragment.getContext(), ShareActivity.class);
        intent.putExtra(ShareActivity.TAG_SHARE_ITEM, item);
        fragment.startActivityForResult(intent, REQUEST_ACTIVITY_SHARE);
    }

}
