package app.editors.manager.ui.activities.login

import android.os.Bundle
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.login.SplashFragment

class SplashActivity : BaseAppActivity() {

    companion object {
        val TAG: String = SplashActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (isActivityFront) {
            return
        }
        if (savedInstanceState == null) {
            showFragment(SplashFragment.newInstance(), null)
        }
    }
}