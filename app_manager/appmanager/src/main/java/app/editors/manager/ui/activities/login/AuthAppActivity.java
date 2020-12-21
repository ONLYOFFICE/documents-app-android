package app.editors.manager.ui.activities.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import app.editors.manager.R;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.login.AuthPagerFragment;

public class AuthAppActivity extends BaseAppActivity {

    public static final String TAG = AuthAppActivity.class.getSimpleName();

    public static final String ACCOUNT_KEY = "ACCOUNT_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        init(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void init(final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            showFragment(AuthPagerFragment.newInstance(getIntent().getParcelableExtra(ACCOUNT_KEY)), null);
        }
    }

    public static void show(final Activity activity, AccountsSqlData sqlData) {
        final Intent intent = new Intent(activity, AuthAppActivity.class);
        intent.putExtra(ACCOUNT_KEY, sqlData);
        activity.startActivity(intent);
    }

}
