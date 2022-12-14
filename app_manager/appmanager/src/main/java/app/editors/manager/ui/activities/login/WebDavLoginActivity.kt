package app.editors.manager.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import app.documents.core.network.ApiContract
import app.documents.core.webdav.WebDavApi
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.databinding.ActivityWebDavLoginBinding
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.storages.dropbox.dropbox.login.DropboxLoginHelper
import app.editors.manager.storages.googledrive.ui.fragments.GoogleDriveSignInFragment
import app.editors.manager.storages.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.storages.onedrive.ui.fragments.OneDriveSignInFragment
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.login.WebDavSignInFragment
import app.editors.manager.ui.interfaces.WebDavInterface
import lib.toolkit.base.managers.utils.getSerializable
import javax.inject.Inject

class WebDavLoginActivity : BaseAppActivity(), WebDavInterface {

    companion object {
        private const val KEY_PROVIDER = "KEY_PROVIDER"
        private const val KEY_ACCOUNT = "KEY_ACCOUNT "
        private const val KEY_TAG_FRAGMENT = "KEY_TAG_FRAGMENT"

        const val ONEDRIVE_FRAGMENT_VALUE = 1
        const val DROPBOX_FRAGMENT_VALUE = 2
        const val GOOGLEDRIVE_FRAGMENT_VALUE = 3

        @JvmStatic
        fun show(activity: Activity, provider: WebDavApi.Providers? = null, account: String?, fragment: Int = 0) {
            activity.startActivityForResult(Intent(activity, WebDavLoginActivity::class.java).apply {
                putExtra(KEY_PROVIDER, provider)
                putExtra(KEY_ACCOUNT, account)
                putExtra(KEY_TAG_FRAGMENT, fragment)
            }, 5)
        }
    }

    private var viewBinding: ActivityWebDavLoginBinding? = null
    override val isMySection: Boolean = false
    @Inject
    lateinit var dropboxLoginHelper: DropboxLoginHelper

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
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finishWithResult(folder: CloudFolder?) { }

    private fun init(savedInstanceState: Bundle?) {
        setSupportActionBar(viewBinding?.appBarToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = intent.getSerializable(KEY_PROVIDER, WebDavApi.Providers::class.java).name
        }
        if (savedInstanceState == null) showFragment()
    }

    private fun showFragment() {
        when(intent.getIntExtra(KEY_TAG_FRAGMENT, 0)) {
            ONEDRIVE_FRAGMENT_VALUE -> showOneDriveSignInFragment()
            DROPBOX_FRAGMENT_VALUE -> showDropboxSignInFragment()
            GOOGLEDRIVE_FRAGMENT_VALUE -> showGoogleDriveSignInFragment()
            else -> showSignInFragment()
        }
    }

    private fun showSignInFragment() {
        showFragment(
            WebDavSignInFragment.newInstance(intent.getSerializableExtra(KEY_PROVIDER) as WebDavApi.Providers),
            null
        )
    }

    private fun showOneDriveSignInFragment() {
        val storage = Storage(
            OneDriveUtils.ONEDRIVE_STORAGE,
            BuildConfig.ONE_DRIVE_COM_CLIENT_ID,
            BuildConfig.ONE_DRIVE_COM_REDIRECT_URL
        )

        showFragment(
            OneDriveSignInFragment.newInstance(storage),
            OneDriveSignInFragment.TAG
        )
    }

    private fun showDropboxSignInFragment() {
        dropboxLoginHelper.startSignInActivity(this) {
            MainActivity.show(this)
            finish()
        }
    }

    private fun showGoogleDriveSignInFragment() {
        val storage = Storage(
            ApiContract.Storage.GOOGLEDRIVE,
            BuildConfig.GOOGLE_COM_CLIENT_ID,
            BuildConfig.GOOGLE_COM_REDIRECT_URL
        )

        showFragment(
            GoogleDriveSignInFragment.newInstance(storage),
            GoogleDriveSignInFragment.TAG
        )
    }
}