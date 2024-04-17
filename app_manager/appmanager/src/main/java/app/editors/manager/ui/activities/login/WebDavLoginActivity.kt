package app.editors.manager.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.app.App
import app.editors.manager.databinding.ActivityWebDavLoginBinding
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.base.StorageLoginFragment
import app.editors.manager.ui.fragments.login.WebDavSignInFragment
import app.editors.manager.ui.interfaces.WebDavInterface
import lib.toolkit.base.managers.utils.getSerializable

class WebDavLoginActivity : BaseAppActivity(), WebDavInterface {

    companion object {
        private const val KEY_PROVIDER = "KEY_PROVIDER"
        private const val KEY_TAG_FRAGMENT = "KEY_TAG_FRAGMENT"
        const val KEY_ACCOUNT = "KEY_ACCOUNT "

        const val ONEDRIVE_FRAGMENT_VALUE = 1
        const val DROPBOX_FRAGMENT_VALUE = 2
        const val GOOGLEDRIVE_FRAGMENT_VALUE = 3

        @JvmStatic
        fun show(activity: Activity, provider: WebdavProvider? = null, account: String?, fragment: Int = 0) {
            activity.startActivityForResult(Intent(activity, WebDavLoginActivity::class.java).apply {
                putExtra(KEY_PROVIDER, provider)
                putExtra(KEY_ACCOUNT, account)
                putExtra(KEY_TAG_FRAGMENT, fragment)
            }, 5)
        }
    }

    private var viewBinding: ActivityWebDavLoginBinding? = null
    override val isMySection: Boolean = false
    override val isRoomStorage: Boolean = false
    override val title: String? = null
    override val providerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getApp().appComponent.inject(this)
        viewBinding = ActivityWebDavLoginBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        hideKeyboard()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finishWithResult(folder: CloudFolder?) {}

    private fun init(savedInstanceState: Bundle?) {
        setSupportActionBar(viewBinding?.appBarToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = intent.getSerializable(KEY_PROVIDER, WebdavProvider::class.java).name
        }
        if (savedInstanceState == null) showFragment()
    }

    private fun showFragment() {
        when (intent.getIntExtra(KEY_TAG_FRAGMENT, 0)) {
            ONEDRIVE_FRAGMENT_VALUE -> showOneDriveSignInFragment()
            DROPBOX_FRAGMENT_VALUE -> showDropboxSignInFragment()
            GOOGLEDRIVE_FRAGMENT_VALUE -> showGoogleDriveSignInFragment()
            else -> showSignInFragment()
        }
    }

    private fun showSignInFragment() {
        showFragment(
            WebDavSignInFragment.newInstance(
                intent.getSerializable(KEY_PROVIDER, WebdavProvider::class.java),
                intent.getStringExtra(KEY_ACCOUNT),
            ),
            null
        )
    }

    private fun showOneDriveSignInFragment() {
        showFragment(
            StorageLoginFragment.newInstance(OneDriveUtils.storage),
            StorageLoginFragment.TAG
        )
    }

    private fun showDropboxSignInFragment() {
        showFragment(
            StorageLoginFragment.newInstance(DropboxUtils.storage),
            StorageLoginFragment.TAG
        )
    }

    private fun showGoogleDriveSignInFragment() {
        showFragment(
            StorageLoginFragment.newInstance(GoogleDriveUtils.storage),
            StorageLoginFragment.TAG
        )
    }
}