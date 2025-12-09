package app.editors.manager.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import app.documents.core.model.cloud.WebdavProvider
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.login.NextCloudWebLoginFragment
import app.editors.manager.ui.fragments.login.OwnCloudWebLoginFragment

class WebViewCloudLoginActivity : BaseAppActivity() {

    companion object {
        val TAG: String = WebViewCloudLoginActivity::class.java.simpleName

        private const val REQUEST_WEBVIEW_LOGIN = 100
        private const val KEY_PORTAL = "KEY_PORTAL"
        private const val KEY_PROVIDER = "KEY_PROVIDER"
        private const val KEY_CONFIG = "KEY_CONFIG"

        @JvmStatic
        fun show(activity: Activity, portal: String?, provider: String?, config: String? = null) {
            activity.startActivityForResult(Intent(activity, WebViewCloudLoginActivity::class.java).apply {
                putExtra(KEY_PORTAL, portal)
                putExtra(KEY_PROVIDER, provider)
                putExtra(KEY_CONFIG, config)
            }, REQUEST_WEBVIEW_LOGIN)
        }
    }

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_cloud_login)
        toolbar = findViewById(R.id.app_bar_toolbar)
        setSupportActionBar(toolbar)
        init()
    }

    private fun init() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val provider = intent.getStringExtra(KEY_PROVIDER).orEmpty()
        val portal = intent.getStringExtra(KEY_PORTAL).orEmpty()
        if (provider == WebdavProvider.OwnCloud.name) {
            showFragment(
                OwnCloudWebLoginFragment.newInstance(
                    authUrl = portal,
                    config = intent.getStringExtra(KEY_CONFIG).orEmpty()
                ),
                OwnCloudWebLoginFragment.TAG
            )
        } else {
            showFragment(
                NextCloudWebLoginFragment.newInstance(portal),
                NextCloudWebLoginFragment.TAG
            )
        }

    }
}