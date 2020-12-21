package app.editors.manager.ui.activities.login;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.activities.main.OnBoardingActivity;
import app.editors.manager.ui.fragments.login.PortalsPagerFragment;
import app.editors.manager.ui.fragments.main.CloudsFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.FragmentUtils;

public class PortalsActivity extends BaseAppActivity implements View.OnClickListener {

    public static final String TAG = PortalsActivity.class.getSimpleName();
    public static final String TAG_ACTION_MESSAGE = "TAG_ACTION_MESSAGE";
    public static final String TAG_MESSAGE = "TAG_MESSAGE";
    public static final String TAG_PORTAL = "TAG_PORTAL";
    public static final String KEY_PORTALS = "KEY_PORTALS";

    private Unbinder mUnbinder;
    @BindView(R.id.app_bar_toolbar)
    protected Toolbar mAppBarToolbar;
    @BindView(R.id.tab_layout)
    protected TabLayout mTabLayout;
    @BindView(R.id.tab_container)
    protected FrameLayout mTabsContainer;

    @Inject
    protected PreferenceTool mPreferenceTool;

    private Fragment mSocialFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portals);
        App.getApp().getAppComponent().inject(this);
        mUnbinder = ButterKnife.bind(this);
        init(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getIntent().getExtras() != null) {
            getIntent().getExtras().clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSocialFragment != null) {
            mSocialFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        SignInActivity.showPortalSignIn(this, mPreferenceTool.getPortal(), mPreferenceTool.getLogin());
    }

    private void init(@Nullable final Bundle savedInstanceState) {
        setSupportActionBar(mAppBarToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mAppBarToolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (getIntent().getBooleanExtra(KEY_PORTALS, false)) {
            initPortals();
        } else {

            getMessage(savedInstanceState);
            showActivities(savedInstanceState);
        }
    }

    private void initPortals() {
        getSupportActionBar().setTitle(R.string.fragment_clouds_title);
        mTabsContainer.setVisibility(View.GONE);
        FragmentUtils.showFragment(getSupportFragmentManager(), CloudsFragment.newInstance(true), R.id.frame_container);
    }

    private void getMessage(@Nullable final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            if (intent != null) {
                if (intent.hasExtra(TAG_MESSAGE)) {
                    final String message = intent.getStringExtra(TAG_MESSAGE);
                    if (message != null && !message.isEmpty()) {
                        if (intent.hasExtra(TAG_ACTION_MESSAGE)) {
                            showSnackBar(message, intent.getStringExtra(TAG_ACTION_MESSAGE), this);
                        } else {
                            showSnackBar(message);
                        }
                    }
                }
            }
        }
    }

    private void showActivities(@Nullable final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            showFragment(PortalsPagerFragment.newInstance(), null);
        }

        if (!mPreferenceTool.getOnBoarding()) {
            OnBoardingActivity.show(this);
        }
    }

    public void setOnActivityResult(Fragment fragment) {
        mSocialFragment = fragment;
    }

    public TabLayout getTabLayout() {
        return mTabLayout;
    }

    public static void show(final Activity context) {
        final Intent intent = new Intent(context, PortalsActivity.class);
        context.startActivityForResult(intent, REQUEST_ACTIVITY_PORTAL);
    }

    public static void showPortals(final Activity context) {
        final Intent intent = new Intent(context, PortalsActivity.class);
        intent.putExtra(KEY_PORTALS, true);
        context.startActivityForResult(intent, REQUEST_ACTIVITY_PORTAL);
    }

}
