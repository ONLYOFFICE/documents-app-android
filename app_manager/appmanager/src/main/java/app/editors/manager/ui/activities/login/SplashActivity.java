package app.editors.manager.ui.activities.login;

import android.os.Bundle;

import app.editors.manager.R;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.login.SplashFragment;

public class SplashActivity extends BaseAppActivity {

    public static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (isActivityFront()) {
            return;
        }

        if (savedInstanceState == null) {
            showFragment(SplashFragment.newInstance(), null);
        }

    }

}
