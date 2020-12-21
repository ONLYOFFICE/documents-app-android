package app.editors.manager.ui.activities.login;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import app.editors.manager.R;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.login.EnterpriseCreatePortalFragment;
import app.editors.manager.ui.fragments.login.EnterprisePhoneFragment;
import app.editors.manager.ui.fragments.login.EnterpriseSignInFragment;
import app.editors.manager.ui.fragments.login.EnterpriseSmsFragment;
import app.editors.manager.ui.fragments.login.PersonalSignUpFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SignInActivity extends BaseAppActivity {

    public static final String TAG = SignInActivity.class.getSimpleName();

    public static final int REQUEST_SIGN_IN = 100;

    public static final String TAG_ACTION = "TAG_ACTION";
    public static final String TAG_PORTAL_SIGN_IN = "TAG_PORTAL_SIGN_IN";
    public static final String TAG_PORTAL_SIGN_IN_EMAIL = "TAG_PORTAL_SIGN_IN_EMAIL";
    public static final String TAG_PORTAL_CREATE = "TAG_PORTAL_CREATE";
    public static final String TAG_PERSONAL_SIGN_UP = "TAG_PERSONAL_SIGN_UP";
    public static final String TAG_SMS = "TAG_SMS";
    public static final String TAG_PHONE = "TAG_PHONE";

    public static final String KEY_PORTAL = "KEY_PORTAL";
    public static final String KEY_LOGIN = "KEY_LOGIN";

    @BindView(R.id.app_bar_toolbar)
    protected Toolbar mAppBarToolbar;

    private Fragment mSocialFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        ButterKnife.bind(this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSocialFragment != null) {
            mSocialFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void init(@Nullable final Bundle savedInstanceState) {
        setSupportActionBar(mAppBarToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getAction(savedInstanceState);
    }

    private void getAction(@Nullable final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            final String action = intent.getStringExtra(TAG_ACTION);
            if (action != null) {
                switch (action) {
                    case TAG_PORTAL_SIGN_IN:
                        showFragment(EnterpriseSignInFragment.newInstance(intent.getStringExtra(KEY_PORTAL),
                                intent.getStringExtra(KEY_LOGIN)), null);
                        break;
                    case TAG_PORTAL_CREATE:
                        showFragment(EnterpriseCreatePortalFragment.newInstance(), null);
                        break;
                    case TAG_PERSONAL_SIGN_UP:
                        showFragment(PersonalSignUpFragment.newInstance(), null);
                        break;
                    case TAG_SMS:
                        showFragment(EnterpriseSmsFragment.newInstance(false, null), null);
                        break;
                    case TAG_PHONE:
                        showFragment(EnterprisePhoneFragment.newInstance(), null);
                        break;
                }
            }
        }
    }

    public void setOnActivityResult(Fragment fragment) {
        mSocialFragment = fragment;
    }

    public static void showPortalSignIn(final Context context, final String portal, final String login) {
        final Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(KEY_PORTAL, portal);
        intent.putExtra(KEY_LOGIN, login);
        intent.putExtra(SignInActivity.TAG_ACTION, SignInActivity.TAG_PORTAL_SIGN_IN);
        context.startActivity(intent);
    }

    public static void showPortalSignIn(final Fragment fragment, final String portal, final String login) {
        final Intent intent = new Intent(fragment.getContext(), SignInActivity.class);
        intent.putExtra(KEY_PORTAL, portal);
        intent.putExtra(KEY_LOGIN, login);
        intent.putExtra(SignInActivity.TAG_ACTION, SignInActivity.TAG_PORTAL_SIGN_IN);
        fragment.startActivityForResult(intent, REQUEST_SIGN_IN);
    }

    public static void showPortalSignInEmail(final Context context) {
        final Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(SignInActivity.TAG_ACTION, SignInActivity.TAG_PORTAL_SIGN_IN);
        intent.putExtra(SignInActivity.TAG_PORTAL_SIGN_IN_EMAIL, SignInActivity.TAG_PORTAL_SIGN_IN_EMAIL);
        context.startActivity(intent);
    }

    public static void showPortalCreate(final Context context) {
        final Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(SignInActivity.TAG_ACTION, SignInActivity.TAG_PORTAL_CREATE);
        context.startActivity(intent);
    }

    public static void showPersonalSignUp(final Context context) {
        final Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(SignInActivity.TAG_ACTION, SignInActivity.TAG_PERSONAL_SIGN_UP);
        context.startActivity(intent);
    }

    public static void showSms(final Context context) {
        final Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(SignInActivity.TAG_ACTION, SignInActivity.TAG_SMS);
        context.startActivity(intent);
    }

    public static void showPhone(final Context context) {
        final Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(SignInActivity.TAG_ACTION, SignInActivity.TAG_PHONE);
        context.startActivity(intent);
    }

}
