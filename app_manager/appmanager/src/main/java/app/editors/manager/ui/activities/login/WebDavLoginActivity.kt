package app.editors.manager.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import app.documents.core.webdav.WebDavApi
import app.editors.manager.databinding.ActivityWebDavLoginBinding
import app.editors.manager.managers.utils.isVisible
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.login.WebDavSignInFragment
import app.editors.manager.ui.interfaces.WebDavInterface

class WebDavLoginActivity : BaseAppActivity(), WebDavInterface {

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

    private var viewBinding: ActivityWebDavLoginBinding? = null
    override val isMySection: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityWebDavLoginBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        hideKeyboard()
        finish()
    }

    override fun showConnectButton(isShow: Boolean) {
        viewBinding?.appBarToolbarConnectButton?.isVisible = isShow
    }

    override fun enableConnectButton(isEnable: Boolean) {
        viewBinding?.appBarToolbarConnectButton?.isEnabled = isEnable
    }

    override fun setOnConnectButtonClickListener(onClick: () -> Unit) {
        viewBinding?.appBarToolbarConnectButton?.setOnClickListener { onClick() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finishWithResult(folder: CloudFolder?) { }

    private fun init(savedInstanceState: Bundle?) {
        setSupportActionBar(viewBinding?.appBarToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
            it.title = (intent.getSerializableExtra(KEY_PROVIDER) as WebDavApi.Providers).name
        }

        if (savedInstanceState == null) showSignInFragment()
    }

    private fun showSignInFragment() {
        showFragment(
            WebDavSignInFragment.newInstance(intent.getSerializableExtra(KEY_PROVIDER) as WebDavApi.Providers),
            null
        )
    }
}