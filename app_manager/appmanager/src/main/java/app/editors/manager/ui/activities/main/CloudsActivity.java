package app.editors.manager.ui.activities.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import app.editors.manager.R;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.main.CloudsFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.FragmentUtils;

public class CloudsActivity extends BaseAppActivity {

    @BindView(R.id.appToolbar)
    Toolbar mToolbar;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clouds_activity_layout);
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
            getSupportActionBar().setTitle(R.string.fragment_clouds_title);
            mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void showFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CloudsFragment.TAG);
        if (fragment == null) {
            fragment = CloudsFragment.newInstance(true);
        }
        FragmentUtils.showFragment(getSupportFragmentManager(), fragment, R.id.frame_container);
    }


    public static void show(Context context) {
        final Intent intent = new Intent(context, CloudsActivity.class);
        context.startActivity(intent);
    }
}
