package app.editors.manager.ui.activities.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import app.editors.manager.R;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.fragments.onboarding.OnBoardingPagerFragment;

public class OnBoardingActivity extends BaseAppActivity {

    public static final String TAG = OnBoardingActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        init(savedInstanceState);
    }

    private void init(final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            showFragment(OnBoardingPagerFragment.newInstance(), null);
        }
    }

    public static void show(final Activity activity) {
        final Intent intent = new Intent(activity, OnBoardingActivity.class);
        activity.startActivityForResult(intent, REQUEST_ACTIVITY_ONBOARDING);
    }

}
