package app.editors.manager.ui.activities.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import app.editors.manager.R;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.main.AppSettingsFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import lib.toolkit.base.managers.utils.FragmentUtils;

public class SettingsActivity extends BaseAppActivity {

    public static final String TAG = SettingsActivity.class.getSimpleName();

    @BindView(R.id.app_bar_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        initToolbar();
        showFragment();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.settings_item_title);
        }
    }

    private void showFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(AppSettingsFragment.TAG);
        if (fragment == null) {
            fragment = AppSettingsFragment.newInstance();
        }
        FragmentUtils.showFragment(getSupportFragmentManager(), fragment, R.id.frame_container);
    }

    public static void show(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }
}
