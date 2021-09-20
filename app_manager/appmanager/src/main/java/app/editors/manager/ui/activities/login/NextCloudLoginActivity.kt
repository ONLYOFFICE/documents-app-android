package app.editors.manager.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.login.NextCloudLoginFragment

class NextCloudLoginActivity : BaseAppActivity() {

    companion object {
        val TAG: String = NextCloudLoginActivity::class.java.simpleName

        private const val REQUEST_NEXTCLOUD_LOGIN = 100
        private const val KEY_PORTAL = "KEY_PORTAL"

        @JvmStatic
        fun show(activity: Activity, portal: String?) {
            activity.startActivityForResult(Intent(activity, NextCloudLoginActivity::class.java).apply {
                putExtra(KEY_PORTAL, portal)
            }, REQUEST_NEXTCLOUD_LOGIN)
        }
    }

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next_cloud_login)
        toolbar = findViewById(R.id.app_bar_toolbar)
        setSupportActionBar(toolbar)
        init()
    }

    private fun init() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        showFragment(NextCloudLoginFragment.newInstance(intent.getStringExtra(KEY_PORTAL)), NextCloudLoginFragment.TAG)
    }
}