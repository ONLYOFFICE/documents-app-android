package app.editors.manager.ui.activities.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import app.editors.manager.R;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.main.ProfileFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.FragmentUtils;

public class ProfileActivity extends BaseAppActivity {

    public static final String TAG = ProfileActivity.class.getSimpleName();
    public static final int REQUEST_PROFILE = 1001;

    @BindView(R.id.appToolbar)
    Toolbar mToolbar;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mUnbinder = ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    private void init() {
        initToolbar();
        showFragment();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.fragment_profile_title);
            mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void showFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ProfileFragment.TAG);
        if (fragment == null) {
            fragment = ProfileFragment.newInstance(getIntent().getParcelableExtra(ProfileFragment.KEY_ACCOUNT));
        }
        FragmentUtils.showFragment(getSupportFragmentManager(), fragment, R.id.frame_container);
    }


    public static void show(Activity activity, AccountsSqlData account) {
        final Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra(ProfileFragment.KEY_ACCOUNT, account);
        activity.startActivityForResult(intent, REQUEST_PROFILE);
    }

}
