package app.editors.manager.ui.activities.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import app.editors.manager.R;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.login.NextCloudLoginFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NextCloudLoginActivity extends BaseAppActivity {

    public static final String TAG = NextCloudLoginActivity.class.getSimpleName();

    public static final int REQUEST_NEXTCLOUD_LOGIN = 100;
    private static final String KEY_PORTAL = "KEY_PORTAL";

    @BindView(R.id.app_bar_toolbar)
    Toolbar mAppBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_cloud_login);
        ButterKnife.bind(this);
        setSupportActionBar(mAppBarToolbar);
        init();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void init() {
        mAppBarToolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        showFragment(NextCloudLoginFragment.newInstance(getIntent().getStringExtra(KEY_PORTAL)), NextCloudLoginFragment.TAG);
    }

    public static void show(Activity activity, String portal) {
        Intent intent = new Intent(activity, NextCloudLoginActivity.class);
        intent.putExtra(KEY_PORTAL, portal);
        activity.startActivityForResult(intent, REQUEST_NEXTCLOUD_LOGIN);
    }
}
