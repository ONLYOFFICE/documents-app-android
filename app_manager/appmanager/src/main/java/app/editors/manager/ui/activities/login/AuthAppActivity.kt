package app.editors.manager.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.login.AuthPagerFragment

class AuthAppActivity : BaseAppActivity() {

    companion object {
        val TAG: String = AuthAppActivity::class.java.simpleName

        const val REQUEST_KEY = "ACCOUNT_KEY"
        const val TFA_KEY = "TFA_KEY"

        fun show(activity: Activity, request: String, key: String) {
            activity.startActivity(Intent(activity, AuthAppActivity::class.java).apply {
                putExtra(REQUEST_KEY, request)
                putExtra(TFA_KEY, key)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            showFragment(AuthPagerFragment.newInstance(intent.getStringExtra(REQUEST_KEY), intent.getStringExtra(TFA_KEY)), null)
        }
    }
}