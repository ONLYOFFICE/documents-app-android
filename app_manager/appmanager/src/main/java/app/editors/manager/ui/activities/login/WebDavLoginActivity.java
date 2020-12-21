package app.editors.manager.ui.activities.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import app.editors.manager.R;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.login.WebDavSignInFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class WebDavLoginActivity extends BaseAppActivity {

    private static final String KEY_PROVIDER = "KEY_PROVIDER";
    private static final String KEY_ACCOUNT = "KEY_ACCOUNT ";

    @BindView(R.id.app_bar_toolbar)
    Toolbar mAppBarToolbar;

    private WebDavApi.Providers mProvider;
    private AccountsSqlData mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_dav_login);
        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().getSerializableExtra(KEY_PROVIDER) != null) {
            mProvider = (WebDavApi.Providers) getIntent().getSerializableExtra(KEY_PROVIDER);
            mAccount = getIntent().getParcelableExtra(KEY_ACCOUNT);
        }

        setSupportActionBar(mAppBarToolbar);
        init();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard();
        finish();
    }

    private void init() {
        mAppBarToolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.login_web_dav_title, mProvider.name()));
        }

        showSignInFragment();
    }

    private void showSignInFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(WebDavSignInFragment.TAG);
        if (fragment == null) {
            fragment = WebDavSignInFragment.newInstance(mProvider, mAccount);
        }
        showFragment(fragment, null);
    }

    public static void show(Activity activity, WebDavApi.Providers provider, AccountsSqlData account) {
        Intent intent = new Intent(activity, WebDavLoginActivity.class);
        intent.putExtra(KEY_PROVIDER, provider);
        intent.putExtra(KEY_ACCOUNT, account);
        activity.startActivityForResult(intent, 5);
    }

}
