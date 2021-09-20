package app.editors.manager.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.login.WebDavSignInFragment

class WebDavLoginActivity : BaseAppActivity() {

    companion object {
        private const val KEY_PROVIDER = "KEY_PROVIDER"
        private const val KEY_ACCOUNT = "KEY_ACCOUNT "

        @JvmStatic
        fun show(activity: Activity, provider: WebDavApi.Providers?, account: String?) {
            activity.startActivityForResult(Intent(activity, WebDavLoginActivity::class.java).apply {
                putExtra(KEY_PROVIDER, provider)
                putExtra(KEY_ACCOUNT, account)
            }, 5)
        }
    }

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_dav_login)

        toolbar = findViewById(R.id.app_bar_toolbar)
        setSupportActionBar(toolbar)

        init(savedInstanceState)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        hideKeyboard()
        finish()
    }

    private fun init(savedInstanceState: Bundle?) {
        toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            intent.let { data ->
                it.title = getString(
                    R.string.login_web_dav_title,
                    (data.getSerializableExtra(KEY_PROVIDER) as WebDavApi.Providers).name
                )
            }

        }
        savedInstanceState?.let {
            // Nothing
        } ?: run {
            showSignInFragment()
        }
    }

    private fun showSignInFragment() {
        showFragment(
            WebDavSignInFragment.newInstance(intent.getSerializableExtra(KEY_PROVIDER) as WebDavApi.Providers),
            null
        )
    }
}