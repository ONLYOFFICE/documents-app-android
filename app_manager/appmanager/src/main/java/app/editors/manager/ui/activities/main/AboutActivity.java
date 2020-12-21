package app.editors.manager.ui.activities.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import app.editors.manager.R;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.main.AboutFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.UiUtils;

public class AboutActivity extends BaseAppActivity {

    public static final String TAG = AboutActivity.class.getSimpleName();

    @BindView(R.id.app_bar_toolbar)
    protected Toolbar mAppBarToolbar;

    private Unbinder mUnbinder;
    private Drawable mCloseDrawable;
    private Drawable mBackDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mUnbinder = ButterKnife.bind(this);
        init(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onBackStackChanged() {
        super.onBackStackChanged();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportActionBar().setHomeAsUpIndicator(mBackDrawable);
        } else {
            getSupportActionBar().setHomeAsUpIndicator(mCloseDrawable);
        }
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

    private void init(final Bundle savedInstanceState) {
        setSupportActionBar(mAppBarToolbar);
        mBackDrawable = UiUtils.getFilteredDrawable(this, R.drawable.ic_toolbar_back, R.color.colorWhite);
        mCloseDrawable = UiUtils.getFilteredDrawable(this, R.drawable.ic_toolbar_close, R.color.colorWhite);
        onBackStackChanged();

        if (savedInstanceState == null) {
            showFragment(AboutFragment.newInstance(), null);
        }
    }

    public static void show(final Context context) {
        final Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

}
