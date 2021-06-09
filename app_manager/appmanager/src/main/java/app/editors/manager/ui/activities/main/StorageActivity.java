package app.editors.manager.ui.activities.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.CloudFolder;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.storage.SelectFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class StorageActivity extends BaseAppActivity {

    public static final String TAG = StorageActivity.class.getSimpleName();
    public static final String TAG_SECTION = "TAG_SECTION";
    public static final String TAG_RESULT = "TAG_RESULT";

    protected Unbinder mUnbinder;
    @BindView(R.id.app_layout)
    protected CoordinatorLayout mAppLayout;
    @BindView(R.id.app_bar_layout)
    protected AppBarLayout mAppBarLayout;
    @BindView(R.id.app_bar_toolbar)
    protected Toolbar mAppBarToolbar;
    @BindView(R.id.frame_container)
    protected FrameLayout mFrameContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        mUnbinder = ButterKnife.bind(this);
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
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    private void init(final Bundle savedInstanceState) {
        setSupportActionBar(mAppBarToolbar);
        if (savedInstanceState == null) {
            showFragment(SelectFragment.newInstance(), null);
        }
        setFinishOnTouchOutside(true);
    }

    public void finishWithResult(final CloudFolder folder) {
        final Intent intent = new Intent();
        intent.putExtra(TAG_RESULT, folder);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public boolean isMySection() {
        final Intent intent = getIntent();
        if ( intent.hasExtra(TAG_SECTION)) {
            return intent.getBooleanExtra(TAG_SECTION, false);
        } else {
            throw new RuntimeException(StorageActivity.class.getSimpleName() + " - must open with extra: " + TAG_SECTION);
        }
    }

    public static void show(final Fragment fragment, final boolean isMySection) {
        final Intent intent = new Intent(fragment.getContext(), StorageActivity.class);
        intent.putExtra(StorageActivity.TAG_SECTION, isMySection);
        fragment.startActivityForResult(intent, REQUEST_ACTIVITY_STORAGE);
    }

}
