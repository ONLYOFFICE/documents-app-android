package app.editors.manager.ui.activities.login

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.ActivityPortalsBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.activities.main.OnBoardingActivity
import app.editors.manager.ui.fragments.login.EnterprisePortalFragment
import app.editors.manager.ui.fragments.main.CloudsFragment
import lib.toolkit.base.managers.utils.AccountUtils
import javax.inject.Inject

class PortalsActivity : BaseAppActivity(), View.OnClickListener {

    companion object {
        val TAG: String = PortalsActivity::class.java.simpleName

        const val TAG_ACTION_MESSAGE = "TAG_ACTION_MESSAGE"
        const val TAG_MESSAGE = "TAG_MESSAGE"
        const val KEY_PORTALS = "KEY_PORTALS"

        @JvmStatic
        fun show(context: Activity) {
            context.startActivityForResult(
                Intent(context, PortalsActivity::class.java),
                REQUEST_ACTIVITY_PORTAL
            )
        }

        fun showPortals(fragment: Fragment) {
            val intent = Intent(fragment.requireContext(), PortalsActivity::class.java)
            intent.putExtra(KEY_PORTALS, true)
            fragment.startActivityForResult(intent, REQUEST_ACTIVITY_PORTAL)
        }

        fun getAddAccountIntent(
            context: Context,
            accountType: String?,
            authType: String?,
            response: AccountAuthenticatorResponse?
        ): Intent {
            val intent = Intent(context, PortalsActivity::class.java)
            intent.putExtra(AccountUtils.KEY_ACCOUNT_TYPE, accountType)
            intent.putExtra(AccountUtils.KEY_AUTH_TYPE, authType)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            return intent
        }
    }


    @Inject
    lateinit var preferenceTool: PreferenceTool

    private var socialFragment: Fragment? = null

    private var viewBinding: ActivityPortalsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getApp().appComponent.inject(this)
        App.getApp().refreshLoginComponent(null)
        viewBinding = ActivityPortalsBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        init(savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        intent.extras?.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        socialFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View) {
        SignInActivity.showPortalSignIn(this, accountOnline?.portalUrl, null, arrayOf())
    }

    private fun init(savedInstanceState: Bundle?) {
        setSupportActionBar(viewBinding?.appBarToolbar)
        viewBinding?.appBarToolbar?.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        if (intent.getBooleanExtra(KEY_PORTALS, false)) {
            initPortals()
        } else {
            getMessage(savedInstanceState)
            showActivities(savedInstanceState)
        }
    }

    private fun getMessage(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            if (intent.hasExtra(TAG_MESSAGE)) {
                val message = intent.getStringExtra(TAG_MESSAGE)
                if (!message.isNullOrEmpty()) {
                    if (intent.hasExtra(TAG_ACTION_MESSAGE)) {
                        showSnackBar(message, intent.getStringExtra(TAG_ACTION_MESSAGE), this)
                    } else {
                        showSnackBar(message)
                    }
                }
            }
        }
    }

    private fun initPortals() {
        supportActionBar?.setTitle(R.string.fragment_clouds_title)
        showFragment(
            CloudsFragment.newInstance(true),
            null
        )
    }

    private fun showActivities(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            showFragment(EnterprisePortalFragment.newInstance(), null)
        }
        if (!preferenceTool.onBoarding) {
            OnBoardingActivity.show(this)
        }
    }

    fun setOnActivityResult(fragment: Fragment?) {
        socialFragment = fragment
    }

}